package core.db

import java.util.UUID

import scalikejdbc.ParameterBinderFactory

trait PostgreSQLExtensions {

  implicit val uuidParameterBinderFactory: ParameterBinderFactory[UUID] =
    ParameterBinderFactory { value => (stmt, idx) =>
      stmt.setObject(idx, value)
    }
}
