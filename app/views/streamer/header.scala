package views.html.streamer

import controllers.routes
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }

object header:

  import trans.streamer.*

  def apply(s: lila.streamer.Streamer.WithUserAndStream, modView: Boolean = false)(using ctx: PageContext) =
    val isMe  = ctx is s.streamer
    val isMod = isGranted(_.ModLog)
    div(cls := "streamer-header")(
      picture.thumbnail(s.streamer, s.user),
      div(cls := "overview")(
        bits.streamerTitle(s),
        s.streamer.headline.map(_.value).map { d =>
          p(cls := s"headline ${
              if d.length < 60 then "small" else if d.length < 120 then "medium" else "large"
            }")(
            d
          )
        },
        ul(cls := "services")(
          s.streamer.twitch.map { twitch =>
            li(
              a(
                cls := List(
                  "service twitch" -> true,
                  "live"           -> s.stream.exists(_.twitch)
                ),
                href := twitch.fullUrl
              )(twitch.minUrl)
            )
          },
          s.streamer.youTube.map { youTube =>
            li(
              a(
                cls := List(
                  "service youTube" -> true,
                  "live"            -> s.stream.exists(_.youTube)
                ),
                href := youTube.fullUrl
              )(youTube.minUrl)
            )
          }
        ),
        span(
          div(cls := "ats")(
            s.stream.map { s =>
              p(cls := "at")(currentlyStreaming(strong(s.status)))
            } getOrElse frag(
              p(cls := "at")(trans.lastSeenActive(momentFromNow(s.streamer.seenAt))),
              s.streamer.liveAt.map { liveAt =>
                p(cls := "at")(lastStream(momentFromNow(liveAt)))
              }
            )
          ),
          s.streamer.youTube.isDefined && s.stream.isEmpty && (isMe || isMod) option
            form(
              action := routes.Streamer.checkOnline(s.streamer._id.value).url,
              method := "post"
            )(input(cls := "button online-check", tpe := "submit", value := "force online check"))
        ),
        div(cls := "streamer-footer")(
          !modView option bits.subscribeButtonFor(s),
          bits.streamerProfile(s)
        )
      )
    )
