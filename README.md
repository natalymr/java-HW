# Java 
----
## Task description
-----
Реализовать систему контроля версий и CLI к ней со следующими возможностями:

* init
* commit <message> <files> с проставлением даты и времени
* reset <to_revision>
* log [from_revision]
* checkout <revision>

__Примечания__
<smth> означает, что передаваемые данные обязательны
[smth] означает, что передаваемые данные опциональны
не накладывается никаких ограничений на хранимые на диске данные и их формат
разрешается использование вспомогательных библиотек
удаление файлов можно не рассматривать
достаточно поддержать версионирование только текстовых файлов
