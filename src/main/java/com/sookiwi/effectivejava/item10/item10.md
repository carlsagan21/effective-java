# `equals`를 오버라이딩 할때는 일반적인 규약을 준수하라

문제가 생기지 않는 가장 좋은 방법은 오버라이딩을 하지 않는 것이다. 그러면 자기 자신과만 같은 것으로 간주된다. 다음과 같은 조건 하에서는 이게 옳은 방법이다.

- **클래스의 각 인스턴스가 원래부터 고유하다.** `Thread`같이 value 라기 보다 활동을 표현할 때.
- **논리적 동일성을 제공할 필요가 없을 때.** 만들 수는 있지만, `java.utils.regex.Pattern` 처럼 논리적 동일성이 클라이언트에 의해 굳이 사용될 필요가 없을 수 있다.
- **슈퍼클래스가 이미 오버라이딩 했으면 밑에서는 할 필요 없다.** `Set` 이 대표적.
- **클래스가 접근 범위가 public이 아니라서 다른 데서 이퀄을 부를 필요가 없다는 것이 확실하면 할 필요 없다.** 아예 `equals`를 부르지 못하게 하려면 다음과 같이 해도 된다.

```java
@Override public boolean equals(Object o) {
    throw new AssertionError(); // Method is never called
}
```

그럼 언제 오버라이딩 하는가? *논리적 동일성* 이 필요할 때 한다. 그래서 주로 `value class`에서 필요하다. `Integer`, `String` 등. 인스턴스를 단일한 것으로 컨트롤하는 경우에는 필요하지 않다. 싱글톤이나 열거형 등.

오버라이딩을 해야하는 경우 일반적인 규약이 정해져있다. `Object` 객체에 따르면 다음과 같다.

- *Reflexive*: 스스로에 대해서 true여야 한다. For any non-null reference value x, x.equals(x) must return true.
- *Symmetric*: 대칭적이어야한다. x == y iff y == x. For any non-null reference values x and y, x.equals(y) must return true if and only if y.equals(x) returns true.
- *Transitive*: 트렌지티브 이퀄리티를 구현해야한다. x == y and y == z => x == z. For any non-null reference values x, y, z, if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) must return true.
- *Consistent*: 항상 동일하게 리턴해야한다. For any non-null reference values x and y, multiple invocations of x.equals(y) must consistently return true or consistently return false, provided no information used in equals comparisons is modified.
- 널과 비교는 항상 false이다. For any non-null reference value x, x.equals(null) must return false.

`Set`, `Map`이 동작하기 위해서는 이런 것이 보장될 필요가 있다.

*equivalence classes*로 객체들을 분할하는 것이다.

Reflexive, Symmetric 예시들은 패키지 내의 코드들을 참조.

`Transitive`위반을 어떻게 해결할 수 있는가? OOP에서 동일성 관계의 근본적인 문제이다. OOP의 추상화 이점을 포기하지 않으려면, **객체를 확장하면서 동일성 규약을 유지하며 값을 추가하는 것은 불가능하다.**

비교 부분에서, `getClass`를 `instanceof`대신 사용해서 피할 수 있다는 말이 있지만 사실이 아니다. `getClass`는 같은 구현체일때만 서로 같다고 하도록 해준다. instanceof 보다 엄밀한 조건으로, Point와 ColorPoint는 `instanceof`에서와 달리 서로 다르다고 판단된다. 하지만 이렇게 하면 Point를 상속하는 객체들은 equal메소드를 사용할 수 없게 된다.
*Liskov substitution principle*에 따르면 타입의 모든 중요한 특성들은 subtype에도 적용되어야만 한다. type을 위한 어떤 메소드도 subtype에서도 동작해야만 한다. `HashSet` 같은 것도 subtype이 맞지 않아서 동작하지 않게 되어버린다.

이 문제에 대한 완벽한 답은 없지만 회피하는 방법은 있다. item18의 "Favor composition over inheritance" 을 참고하라. Point를 상속받기보다, private Point 필드를 주고 ColorPoint와 동일한 위치를 갖는 Point를 리턴하는 public view 메소드를 만드는 것이다. 

`java.sql.Timestamp`는 `java.util.Date`를 상속받아 nanosecond를 추가했다. 이 때문에 Timestamp의 equal 구현은 대칭성을 위반하여, `Date`와 `Timestamp`가 같이 쓰이면 문제가 발생하게 된다. `Timestamp`는 디스클레이머로 알리고는 있다.

abstract class를 상속받아서 value를 추가하는 경우는 동일성 비교에 문제가 없다. 이것은 클래스 위계 정렬에 중요하다. Item23“Prefer class hierarchies to tagged classes.” 참조. (? 완전히 이해 못함)


`Consistent`에 따르면, 뮤터블 객체는 다른 객체들에게 서로 다른 시간에 같다고 판단될 수 있지만, 이뮤터블은 그럴 수 없다. 이뮤터블이 필요한지 깊게 생각해볼 필요가 있다.(item17) 이뮤터블이면, 처음부터 같은 객체는 계속 같아야하고, 처음부터 다른 객체는 계속 달라야한다.

이뮤터블 여부와 무관하게, **믿을 수 없는 자료에 의존하는 equals 메소드를 작성하지 말라.** 그렇지 않으면 일관성을 만족하기가 매우 어렵다. `java.net.URL`의 경우 equals가 IP어드레스에 의존한다. 호스트명을 IP어드레스로 변환시키는 것은 네트워크 엑세스를 요구하는 등, 일관적이기 어렵다. 좋지 못한 구현이지만 호환성 문제로 제거하지는 못하고 있다. 이 문제를 피하기 위해, equals는 메모리에 있는 객체만을 대상으로한 결정론적인 계산에서만 사용될 필요가 있다.

`Non Nullity`. 모든 객체는 null과 같지 않아야 한다. true를 리턴하는 걸 보긴 어렵지만, `NullPointerException`을 리턴하는 것은 충분히 있을 법한 일이다. 대부분의 경우 null 가드를 통해서 이걸 막고 있다.

```java
@Override public boolean equals(Object o) {
    if (o == null)
        return false;
//    ...
}
```

이 가드는 사실 불필요하다. 어짜피 equals를 하려면 instanceof 테스트를 해야하기 때문이다. 거기에서 항상 false일 수 있다.

```java
@Override public boolean equals(Object o) {
    if (!(o instanceof MyType))
        return false;
    MyType mt = (MyType) o;
//    ...
}
```

이 타입체킹이 없으면 `ClassCastException`이 일어나게 되어, 동일성 규약을 깨게 된다.

---

요약해서 고퀄의 equals를 만들려면 다음을 준수해야 한다.
- **아규먼트로 들어온 레퍼런스가 이 객체인지 확인하고 싶으면, `==`를 사용하라.** 성능을 위해서 좋다. 비교가 비쌀 경우에 유용하다.
- **instanceof 를 써서 타입을 비교하라.** 대부분의 경우 비교하는 타입은 그 자신이지만, 때떄로 implements 하는 interface일 때도 있다. 클래스별 비교를 하기 위한 인터페이스가 주어진 경우에는 그렇게 해도 된다. Collection 인터페이스인 `Set`, `ListMap`, `Map.Entry`가 이런 성질을 가진다.
- **아규먼트를 올바른 타입으로 캐스팅하라.** instanceof 로 테스트 되었기 떄문에, 캐스팅 성공이 보장되어 있다.
- **클래스의 "주요(significant)" 필드마다, 필드 값이 같은지 확인하라.** 인터페이스 내에서 비교하는 경우는, 인터페이스에 정의된 필드 접근자를 활용해서 비교하라.

프리미티브 타입에 대해서는 float과 double을 제외하고 == 를 사용하라. 객체이면 equals를 재귀적으로 호출하라. float에 대해서는 Float.compare()를 사용하라. 이는 Float.NaN, -0.0f 때문에 필요하다. Float.equals나 Double.equals를 사용할 수 있지만, 오토박싱이 일어나서 성능에 좋지 않다. 배열에 대해서는 각 엘레먼트 별로 비교하라. 전 요소가 유의미하게 중요하면, Arrays.equals를 사용하라.

객체를 비교할 때, null이 있을 수 있기 떄문에 `Objects.equals(Object, Object)`를 사용하라.

`CaseInsensitiveString`같이 필드 비교가 까다로운 경우가 있다. 이건 필드의 *canonical form*를 저장해서 비교하는게 좋을 수도 있다. 이 테크닉은 이뮤터블 클래스에 가장 적합하다. 오브젝트가 가변적이면, 바뀔 때마다 캐노니컬 폼을 업데이트 해야 하기 떄문이다.

equals의 속도는 필드 비교 순서에 의해 영향받을 수도 있다. 다를 가능성이 높으면서 비교가 간단한 것부터 하자. 객체의 논리적 상태왁 관련없는 필드는 하지 말자. 예를 들자면 locking을 위한 필드. 파생된 필드는 굳이 비교할 필요가 없지만, 그렇게 하는게 성능을 향상시킬 수는 있다. 다각형에서 넓이는 선분이나 점에 의해 파생된 값이지만, 넓이가 다르면 그냥 다른 것이기 때문에 빠르다.

**`equals`를 작성한 다음 스스로에게 질문하라. 대칭적인가? 트랜지티브한가? 컨시스턴트한가?** 유닛테스트를 써서 테스트하라. AutoValue를 써서 equals를 생성한 것이 아니라면. 다른 두 속성은 보통 왠만하면 충족된다.

---

마지막 주의점들
- **equals를 오버라이드 하면 hashcode도 오버라이딩하라**(item11)
- **너무 머리 굴려서 만들려고 하지 마라.** 필드를 단순비교하는 것만 잘해도 만족할 가능성이 높다. 너무 적극적으로 동일성을 정의하려다보면 문제가 생기기 쉽다. alias를 고려하려는 것은 보통 문제가 된다. `File` 같은 클래스는 그러면 안된다.
- **equals 정의에서 Object만 써라** Object가 아니라 다른 걸로 대체하면 보통 문제가 된다.

```java
// Broken - parameter type must be Object!
public boolean equals(MyClass o) {
//    ...
}
```

이건 `Object.equals`를 오버라이딩 하지 못하고 오버로딩한다.[item52] 같이 제공하려는 생각도 하지 마라. 이걸 상속하는 다른 클래스에서 오버라이딩하려 할때 헷갈릴 가능성이 높다. @Overriding을 빼먹지 않고 넣었으면 컴파일이 안되어서 문제가 없다.[item40]

equals와 hashcode를 테스팅하는 것은 지루한 작업이다. AutoValue 프레임워크를 쓰면 그것을 피할 수 있다. IDE 자동생성은 코드가 좀 더 더럽고 필드의 변화를 자동적으로 반영하지 못한다. 수동으로 만들고자 할 때에는 실수를 줄이기 위해 쓰는게 좋긴 하다.

In summary, don’t override the equals method unless you have to: in many cases, the implementation inherited from Object does exactly what you want. If you do override equals, make sure to compare all of the class’s significant fields and to compare them in a manner that preserves all five provisions of the equals contract.
