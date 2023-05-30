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
package wvlet.airframe.http

import wvlet.airframe.http.Http.formatInstant
import wvlet.airframe.http.HttpMessage.{EmptyMessage, Message, StringMessage, extractQueryFromUri}
import wvlet.airframe.msgpack.spi.MsgPack

import java.time.Instant

case class Request(
    method: String = HttpMethod.GET,
    // Path and query string beginning from "/"
    uri: String = "/",
    header: HttpMultiMap = HttpMultiMap.empty,
    message: Message = EmptyMessage,
    remoteAddress: Option[ServerAddress] = None
) {
  override def toString: String = s"Request(${method},${uri},${header})"

  /**
    * URI without query string (e.g., /v1/info)
    */
  def path: String = {
    val u = uri
    u.indexOf("?") match {
      case -1  => u
      case pos => u.substring(0, pos)
    }
  }

  /**
    * Extract the query string parameters as HttpMultiMap
    */
  def query: HttpMultiMap                                      = extractQueryFromUri(uri)
  def withFilter(f: Request => Request): Request               = f(this)
  def withMethod(method: String): Request                      = this.copy(method = method)
  def withUri(uri: String): Request                            = this.copy(uri = uri)
  def withRemoteAddress(remoteAddress: ServerAddress): Request = this.copy(remoteAddress = Some(remoteAddress))

  protected def copyWith(newHeader: HttpMultiMap): Request = this.copy(header = newHeader)
  protected def copyWith(newMessage: Message): Request     = this.copy(message = newMessage)

  // Accessors
  def getHeader(key: String): Option[String] = header.get(key)

  def getAllHeader(key: String): Seq[String] = header.getAll(key)

  def allow: Option[String] = header.get(HttpHeader.Allow)

  def accept: Seq[String] = Http.parseAcceptHeader(header.get(HttpHeader.Accept))

  def authorization: Option[String] = header.get(HttpHeader.Authorization)

  def cacheControl: Option[String] = header.get(HttpHeader.CacheControl)

  def contentType: Option[String] = header.get(HttpHeader.ContentType)

  def contentEncoding: Option[String] = header.get(HttpHeader.ContentEncoding)

  def contentLength: Option[Long] = header.get(HttpHeader.ContentLength).map(_.toLong)

  def date: Option[String] = header.get(HttpHeader.Date)

  def expires: Option[String] = header.get(HttpHeader.Expires)

  def host: Option[String] = header.get(HttpHeader.Host)

  def lastModified: Option[String] = header.get(HttpHeader.LastModified)

  def referer: Option[String] = header.get(HttpHeader.Referer)

  def userAgent: Option[String] = header.get(HttpHeader.UserAgent)

  def xForwardedFor: Option[String] = header.get(HttpHeader.xForwardedFor)

  def xForwardedProto: Option[String] = header.get(HttpHeader.xForwardedProto)

  def withHeader(key: String, value: String): Request = {
    copyWith(header.set(key, value))
  }

  def withHeader(newHeader: HttpMultiMap): Request = {
    copyWith(newHeader)
  }

  def withHeader(f: HttpMultiMap => HttpMultiMap): Request = {
    copyWith(f(header))
  }

  def addHeader(key: String, value: String): Request = {
    copyWith(header.add(key, value))
  }

  def removeHeader(key: String): Request = {
    copyWith(header.remove(key))
  }

  def withContent(content: Message): Request = {
    copyWith(content)
  }

  def withContent(content: String): Request = {
    copyWith(StringMessage(content))
  }

  def withContent(content: Array[Byte]): Request = {
    copyWith(HttpMessage.byteArrayMessage(content))
  }

  def withJson(json: String): Request = {
    copyWith(HttpMessage.stringMessage(json)).withContentTypeJson
  }

  def withJson(json: Array[Byte]): Request = {
    copyWith(HttpMessage.byteArrayMessage(json)).withContentTypeJson
  }

  def withMsgPack(msgPack: MsgPack): Request = {
    copyWith(HttpMessage.byteArrayMessage(msgPack)).withContentTypeMsgPack
  }

  // Content reader
  def contentString: String = {
    message.toContentString
  }

  def contentBytes: Array[Byte] = {
    message.toContentBytes
  }

  // HTTP header setting utility methods
  def withAccept(acceptType: String): Request = withHeader(HttpHeader.Accept, acceptType)

  def withAcceptMsgPack: Request = withHeader(HttpHeader.Accept, HttpHeader.MediaType.ApplicationMsgPack)

  def withAcceptJson: Request = withHeader(HttpHeader.Accept, HttpHeader.MediaType.ApplicationJson)

  def withAllow(allow: String): Request = withHeader(HttpHeader.Allow, allow)

  def withAuthorization(authorization: String): Request = withHeader(HttpHeader.Authorization, authorization)

  def withCacheControl(cacheControl: String): Request = withHeader(HttpHeader.CacheControl, cacheControl)

  def withContentType(contentType: String): Request = withHeader(HttpHeader.ContentType, contentType)

  def withContentTypeJson: Request = withContentType(HttpHeader.MediaType.ApplicationJson)

  def withContentTypeMsgPack: Request = withContentType(HttpHeader.MediaType.ApplicationMsgPack)

  def withContentLength(length: Long): Request = withHeader(HttpHeader.ContentLength, length.toString)

  def withDate(date: String): Request = withHeader(HttpHeader.Date, date)

  def withDate(date: Instant) = withHeader(HttpHeader.Date, formatInstant(date))

  def withExpires(expires: String): Request = withHeader(HttpHeader.Expires, expires)

  def withHost(host: String): Request = withHeader(HttpHeader.Host, host)

  def noHost: Request = removeHeader(HttpHeader.Host)

  def withLastModified(lastModified: String): Request = withHeader(HttpHeader.LastModified, lastModified)

  def withReferer(referer: String): Request = withHeader(HttpHeader.Referer, referer)

  def withUserAgent(userAgent: String): Request = withHeader(HttpHeader.UserAgent, userAgent)

  def withXForwardedFor(xForwardedFor: String): Request = withHeader(HttpHeader.xForwardedFor, xForwardedFor)

  def withXForwardedProto(xForwardedProto: String): Request = withHeader(HttpHeader.xForwardedProto, xForwardedProto)

  def isContentTypeJson: Boolean = {
    contentType.exists(_.startsWith("application/json"))
  }

  def isContentTypeMsgPack: Boolean = {
    contentType.exists(x => x == HttpHeader.MediaType.ApplicationMsgPack || x == "application/x-msgpack")
  }

  def acceptsJson: Boolean = {
    accept.exists(x => x == HttpHeader.MediaType.ApplicationJson || x.startsWith("application/json"))
  }

  def acceptsMsgPack: Boolean = {
    accept.exists(x => x == HttpHeader.MediaType.ApplicationMsgPack || x == "application/x-msgpack")
  }
}
