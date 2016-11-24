package core.db

import java.util.UUID

import scalikejdbc.ParameterBinderFactory

trait PostgreSQLExtensions {

  implicit val intParameterBinderFactory: ParameterBinderFactory[UUID] =
    ParameterBinderFactory { value => (stmt, idx) =>
      stmt.setString(idx, value.toString)
    }
}
