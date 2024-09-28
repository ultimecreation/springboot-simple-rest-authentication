# springboot-simple-rest-authentication

## Packages

- Web
- Oauth2 resource server
- Devtools
- Validation
- Mysql
- Data Jpa

## Development flow

- create `controller package and AccountController class`
- create `register,login and profile methods and endpoints`

- populate `application.properties files with entries for mysql and jwt`

```
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        spring.datasource.url=jdbc:mysql://localhost:3306/authapi
        spring.datasource.username=root
        spring.datasource.password=root

        spring.jpa.show-sql=true
        spring.jpa.hibernate.ddl-auto=update
        # spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

        security.jwt.secret-key=thisisarandomstringforjwt
        security.jwt.issuer=authapi
```

- create `config package and SecurityConfig class`
- populate `AccountController methods along with entities,dtos and Services`
