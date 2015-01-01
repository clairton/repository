# Repositorio Para Facilitar o uso do JPA

DSL para facilitar consulta com o Criteria Builder do JPA.

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
Aplicando Predicados:
```java
final Predicate p1 = new Predicate("Teste", Operacao_.nome);
final Predicate p2 = new Predicate("OutraOperacao", Comparators.NOT_EQUAL,
Operacao_.nome);
repository.from(Operacao.class).where(p1).and(p2).count();
```
Dentro dos Predicados podemos definir o tipo de junção, comparação e operação:

```java
new Predicate(1l, JoinType.INNER, Comparators.EQUAL, Operators.OR,
Operacao_.recurso, Recurso_.id)
```
A operação também pode ser definida com um método na DSL:
```java
final Predicate p1 = new Predicate("Teste", Operacao_.nome);
final Predicate p2 = new Predicate("OutraOperacao", Operacao_.nome);
final Predicate p3 = new Predicate("AindaOutraOperacao", Operacao_.nome);
repository.from(Operacao.class).where(p1).and(p2).or(p3).count();
```
Os atributos são recuperados pelo metamodel do JPA, e devem navegar da
entidade definida no método from, até o atributo em que se deseja que o 
filtro seja aplicado. Por exemplo, tendo os modelos Operação -> Recurso -> Aplicação, e o vamos retorna as aplicações que tem uma operação relacionada com o nome que contém a string "teste", teriamos:
```java
Predicate p = new Predicate("teste", Comparators.LIKE, Aplicacao_.recursos,
    Recurso_.operacao, Operacao_.nome);
repository.from(Aplicacao.class).where(p).list();
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