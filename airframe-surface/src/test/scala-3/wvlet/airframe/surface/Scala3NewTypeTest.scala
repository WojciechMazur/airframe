/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wvlet.airframe.surface
import wvlet.airspec.AirSpec

object Scala3NewTypeTest extends AirSpec:
  trait Label1

  test("support intersection type") {
    val s = Surface.of[String & Label1]
    s.name shouldBe "String&Label1"
    s.fullName shouldBe "java.lang.String&wvlet.airframe.surface.Scala3NewTypeTest.Label1"
    s shouldMatch { case i: IntersectionSurface =>
      i.left shouldBe Surface.of[String]
      i.right shouldBe Surface.of[Label1]
    }
    s shouldNotBe Surface.of[String]
  }

  test("support union type") {
    val s = Surface.of[String | Label1]
    s.name shouldBe "String|Label1"
    s.fullName shouldBe "java.lang.String|wvlet.airframe.surface.Scala3NewTypeTest.Label1"
    s shouldMatch { case i: UnionSurface =>
      i.left shouldBe Surface.of[String]
      i.right shouldBe Surface.of[Label1]
    }
    s shouldNotBe Surface.of[String]
  }
