# 항상 toString을 오버라이드하라

toString은 "사람이 읽기에 쉽도록 정확하고 충분한 정보를 가지고 있는 표현"이어야 한다.

**좋은 toString을 제공하는 것은 당신의 클래스를 더 사용하기 좋게 만들고 그 클래스를 사용하는 시스템이 디버깅하기 쉽도록 한다.**

당신이 toString을 부르지 않을지라도, 다른 누군가가 쓰는 일은 있을 수 있다. 특히 컬렉션에 대해서는 더 그렇다.

**실용적이려면, toString은 오브젝트에 있는 모든 흥미로운 정보들을 리턴해야 한다.** 너무 크거나 스트링으로 표현하기 적합하지 않은 경우들도 있을 수 있는데, 그런 것들은 `Manhattan residential phone directory (1487536 listings)` 나 `Thread[main,5,main]`식으로 표현하는게 낫다. 이상적으로는, 스트링은 self-explanatory이어야 한다.(Thread예제는 만족하지 못한다.)

또 하나 생각해볼 점은 리턴 벨류의 포멧을 문서에 명시할지의 여부이다. 벨류 클래스에 대해서는 그렇게 하는 것이 권장된다. 포멧을 명시하는 것의 장점은, 그것이 표준이면서 모호하지 않고 사람이 읽을수 있는 오브젝트의 표현이 된다는 것이다. 이 스트링 포멧은 다른 프로그램의 인풋 아웃풋으로 사용될수도 있고, CSV같은 포멧으로 저장되기에도 용이하다. 포멧을 명시한 경우, 그것에 해당되는 스테틱 펙토리 함수나 컨스트럭터를 제공해서 프로그래머가 오브젝트와 스트링 표현을 서로 바꿀 수 있게 해주는 것을 추천한다. 이미 자바 라이브러리의 `BigInteger`, `BigDecimal`등 boxed primitives가 그렇게 되어 있다.

포멧을 명시하는 것의 단점은, 한번이라도 그걸 명시하면, 그것이 사용되는 한 수정할 수 없다는 점이다. 프로그래머들이 그것을 파싱하는 프로그램을 짤 것이기 때문이다.

**포멧을 명시할지 말지를 결정하면, 의도를 분명하게 문서에 남겨야한다** 남기기로 했다면 명확하게 해야한다.

```java
/**
 * Returns the string representation of this phone number.
 * The string consists of twelve characters whose format is
 * "XXX-YYY-ZZZZ", where XXX is the area code, YYY is the
 * prefix, and ZZZZ is the line number. Each of the capital
 * letters represents a single decimal digit.
 *
 * If any of the three parts of this phone number is too small
 * to fill up its field, the field is padded with leading zeros.
 * For example, if the value of the line number is 123, the last
 * four characters of the string representation will be "0123".
 */
@Override public String toString() {
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

포멧을 명시하지 않으려면 다음과 같이 하라.

```java
/**
 * Returns a brief description of this potion. The exact details
 * of the representation are unspecified and subject to change,
 * but the following may be regarded as typical:
 *
 * "[Potion #9: type=love, smell=turpentine, look=india ink]"
 */
@Override public String toString() { ... }
```

포멧을 남기든 남기지 않든, **toString으로 리턴된 값에 들어있는 정보에 접근할 수 있는 프로그래매틱한 접근법을 제공하라.** 예를 들어, PhoneNumber는 에어리어 코드, 프리픽스, 라인 넘버에 대한 접근자를 가지고 있어야 한다. 그렇지 않으면, 당신은 프로그래머가 스트링을 파싱하도록 강제하는 것이다. 쓸데없는 일을 줄일수 있는 것은 물론, 접근자를 제공하면 프로그래머의 파서에 의한 에러를 방지할수도 있다. 접근자를 제공하여, 당신은 스트링 포멧을 사실상의 API로 만들 수 있다. 설령 그것이 바뀔수 있다고 명시했더라도 말이다.

스테틱 유틸리티 클래스에 toString을 정의할 이유는 없다. enum은 기본 스트링 변환이 좋아서 필요가 없다. 하지만 추상 클래스는 그것을 상속받는 하위 클래스들이 공유할 수 있는 공통된 toString을 제공하는 것이 중요하다. 컬렉션의 클래스들의 toString은 컬렉션 추상 클래스의 toString을 상속받아 만들어져있다.

AutoValue도 toString을 만들어준다. 이것은 각 필드의 내용을 보여주는데에는 적합하지만, 클래스의 *의미*를 보여주는데 특화되어 있지는 않다. PhoneNumber의 경우 자동 생성 함수는 의미(전화번호는 표준적인 스트링 표현 형태를 이미 의미적으로 가지고 있으니까)를 완전히 담아내지는 못한다. 하지만 Potion 예제에서는 의미를 충분히 담아낸다. 어쨌든 자동 생성 스트링 함수는 기본 스트링 함수보다는 더 좋다.

To recap, override Object’s toString implementation in every instantiable class you write, unless a superclass has already done so. It makes classes much more pleasant to use and aids in debugging. The toString method should return a concise, useful description of the object, in an aesthetically pleasing format.
