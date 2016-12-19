package utils

import java.util.Optional

object JavaConverters {

  implicit class OptionalToOption[A](val o: Optional[A]) extends AnyVal {
    def toScalaOption: Option[A] = if (o.isPresent) Some(o.get) else None
  }
}
