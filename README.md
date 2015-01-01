# Repositorio Para Facilitar o uso do JPA
A instancia de repository pode ser recuperada por CDI.

Para Recuperar o último registro:
```java
repository.from(Aplicacao.class).last();
```
Para Recuperar o primeiro registro:
```java
repository.from(Aplicacao.class).first();
```
Para Recuperar uma Coleção:
```java
repository.from(Aplicacao.class).collection();
```
Para Recuperar uma Coleção pagina:
```java
repository.from(Aplicacao.class).collection(1, 1);
```
Para contar os registros:
```java
repository.from(Aplicacao.class).count();
```
Aplicando Filtros:
```java
final Predicate filtro = new Predicate("Teste", Operacao_.nome);
final Predicate filtro2 = new Predicate("OutraOperacao", Comparators.NOT_EQUAL, Operacao_.nome);
repository.from(Operacao.class).where(filtro).and(filtro2).count();
```


Integrado ao projeto https://github.com/clairton/tenant.

Para usar será necessário adicionar os repositórios maven:

```xml
<repository>
	<id>mvn-repo-releases</id>
	<url>https://raw.github.com/clairton/mvn-repo.git/releases</url>
</repository>
<repository>
	<id>mvn-repo-snapshot</id>
	<url>https://raw.github.com/clairton/mvn-repo.git/snapshots</url>
</repository>
```
 Também adicionar as depêndencias:
```xml
<dependency>
    <groupId>br.eti.clairton</groupId>
	<artifactId>repository</artifactId>
	<version>0.1.0</version>
</dependency>
```