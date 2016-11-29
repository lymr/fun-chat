package core.db

import java.util.UUID

import scalikejdbc.ParameterBinderFactory

trait PostgreSQLExtensions {

  implicit val uuidParameterBinderFactory: ParameterBinderFactory[UUID] =
    ParameterBinderFactory { value => (stmt, idx) =>
      stmt.setString(idx, value.toString)
    }

  implicit val charArrayParameterBinderFactory: ParameterBinderFactory[Array[Char]] =
    ParameterBinderFactory { value => (stmt, idx) =>
      stmt.setString(idx, value.toString)
    }
}
