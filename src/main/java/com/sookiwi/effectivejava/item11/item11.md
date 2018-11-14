# `equals`를 오버라이딩 할때는 `hashcode`도 오버라이드 하라

`HashTable`, `HashSet`을 위해서 필요하다. 해쉬코드의 규약은 다음과 같다.
- 동일성 비교에서 사용된 정보가 바뀌지 않았다는 가정 하에서 동일한 시행 하에서 몇번을 불려도 같은 값을 리턴해야 한다.
- 두 객체가 equals에 의해 같으면, 해쉬코드도 같아야한다
- 두 객체가 equals에 의해 서로 다르더라도, 해쉬코드가 반드시 달라야 하는 것은 아니다. 성능을 위해서는 다른 것이 좋다.

**equals를 오버라이딩하고 hashcode를 오버라이딩 하지 않으면 2번 규약을 위반한다. 서로 논리적으로 같으면 해쉬코드도 같아야 한다.**

```java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "Jenny");
```

여기서 `m.get(new PhoneNumber(707, 867, 5309))`를 불러도 null이 나온다. 서로 다른 객체라서 hashcode가 다르기 때문이다. equals에 의해서는 같지만 hashcode가 다른 것이다. 다른 hash bucket을 볼 수 밖에 없다. 설령 운좋게 같은 hash bucket을 본다고 하더라도, 해쉬코드가 서로 다르면 equals를 하지도 않는다.

그렇다면 어떻게 해시코드를 만들어야 할까? 다음은 규약은 위반하지 않지만 atrocious하다.

```java
@Override public int hashCode() { return 42; }
```

제대로 만드는 방법

1. result라는 int 변수를 만들고, 첫번째 주요한(equals에서 사용되는) 필드로 초기화하라.
2. 다른 주요한 필드들에 대해서
    1. 필드에 대해서 int 해시코드를 계산하는데
        1. 만약 필드가 프리미티브 타입이면, Type.hashcode(f)를 하라. Type은 int 이면 Integer, boolean이면 Boolean
        2. 객체라서 equals이 객체를 재귀적으로 비교하게 되어있으면, hashcode도 재귀적으로 호출하라ㅏ. 더 복잡한 비교가 요구되면, canonical representation을 계산하면 된다. null이면 0으로.
        3. 배열이면, 각 유의미한 원소들을 별개의 필드로 간주하라. 각각 적용해서 값들을 컴바인해야한다. 2.b에서 하는 것 처럼. 중요한 엘레멘트가 없다면, 0이 아닌 상수를 사용해서 표현하라. 만약 모든 원소가 중요하면, Arrays.hashCode를 사용할 수 있다.
    2. 계산된 해시코드 c를 다음과 같이 result에 넣어라: `result = 31 * result + c;`
3. result를 리턴

만들고 나면, equals와 hashcode가 동일한지 스스로 물어봐야 한다. AutoValue를 쓰지 않으면 테스트를 짜야한다.

derived field는 제외해도 된다. equals에서 사용되지 않은 필드는 반드시 제외해야한다.

짝수 소수인 31을 곱하는 과정은 순서에 의해 해시코드가 바뀌게 해서, anagram이 동일한 해시코드를 갖는 것을 막는다. 2를 곱하면 시프팅 하는 것과 마찬가지이므로 홀수를 하는 것이고, 소수를 택하는 이유는 명확하진 않다. 31을 곱하는 것의 장점은 이걸 `31 * i == (i << 5) - i`로 바꿔서 성능을 올릴수 있다는 것이다.

```java
// Typical hashCode method
@Override public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```

충분히 좋긴 하지만, 최선은 아니다. 자바 플랫폼 라이브러리에 준하는 수준이고 일반 유저들에게는 충분하다. 더 알아보려면 Guava’s com.google.common.hash.Hashing 참고.

라이브러리에서 제공하는 기본 해시 함수도 있다. 하지만 속도가 느리므로 성능이 중요하지 않은 경우에만 사용해야한다.

```java
// One-line hashCode method - mediocre performance
@Override public int hashCode() {
   return Objects.hash(lineNum, prefix, areaCode);
}
```

만약 객체가 불변이고 해시 계산이 비싸다면, 캐싱을 고려해볼 수 있다. 주로 해시의 키로 사용되는 경우, 인스턴스가 만들어 질 때 해시를 만들어야 한다. 아니면 해시코드가 요청될 때 lazy하게 초기화해야한다. 단 lazy 초기화를 할때는 쓰레드 세이프가 보장되어 있어야 한다(?)

0으로 초기화하지 않으면 그럴듯한 해시코드와 헷갈릴 수 있다. 아직 해시코드가 없다는 것을 분명히 하기 위해 0을 써야 한다.

```java
// hashCode method with lazily initialized cached hash code
private int hashCode; // Automatically initialized to 0

@Override public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

**성능을 위해서 중요한 필드를 지우지 말라.** Java2에서 스트링에서 그런 문제가 있었다.

**해시코드가 리턴하는 값에 대한 자세한 설명을 줄 필요는 없다. 클라이언트가 의존하지 않으면 수정하기가 쉬워진다.** 실제로 String, Integer는 그런 문제가 있다. 추후에 성능 향상이 어려워졌다.

In summary, you must override hashCode every time you override equals, or your program will not run correctly. Your hashCode method must obey the general contract specified in Object and must do a reasonable job assigning unequal hash codes to unequal instances. This is easy to achieve, if slightly tedious, using the recipe on page 51. As mentioned in Item 10, the AutoValue framework provides a fine alternative to writing equals and hashCode methods manually, and IDEs also provide some of this functionality. 

## 참고

[Guava Hashing Explained](https://github.com/google/guava/wiki/HashingExplained)

[AutoValue generation example](https://github.com/google/auto/blob/1339e4038c23b8d667316ac4f179a8dac35685e4/value/userguide/generated-builder-example.md)

[Lombok vs AutoValue vs Immutable](https://dzone.com/articles/lombok-autovalue-and-immutables)

## 내 생각

해시코드는 equals로부터 derived되는 것인데, 이것을 따로 구현해놓다보니 개념적으로는 동일한 함수들이 별개적으로 구현되거나 빠지는 일이 발생하는 것이다. 이건 자바 언어 디자인 차원에서 hashcode와 equals를 묶어줬어야 했다고 생각한다. 
