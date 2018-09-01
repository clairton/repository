# repository[![Build Status](https://travis-ci.org/clairton/repository.svg?branch=master)](https://travis-ci.org/clairton/repository)

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
Para Recuperar uma Coleção paginada:
```java
PaginatedMetaList<Aplicacao> metaList = repository.from(Aplicacao.class).collection(1, 1);
metaList.getMeta().getTotal();//total de itens em todosd as paginas
metaList.getMeta().getPage();//pagina atual
```
Para Remover uma entidade:
```java
repository.remove(objetoHaSerRemovido);
```
Para Remover uma Coleção:
```java
repository.from(Aplicacao.class).where("Teste", Aplicacao_.nome).remove();
```
Para contar os registros:
```java
repository.from(Aplicacao.class).count();
```
Por padrão os registros são contados usando o distinct, caso não deseje isso use:
```java
repository.from(Aplicacao.class).count(Boolean.FALSE);
```
Para ordenar o resultado:
```java
repository.from(Aplicacao.class).orderBy(Order.Type.DESC, Aplicacao_.nome).list();
```
Para aplicar hint:
```java
repository.hint("org.hibernate.readOnly", true);
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
filtro seja aplicado. Por exemplo, tendo os modelos Operação -> Recurso -> Aplicação,
e o vamos retorna as aplicações que tem uma operação relacionada com o nome que contém a string "teste", teriamos:
```java
Predicate p = new Predicate("teste", Comparators.LIKE, Aplicacao_.recursos,
    Recurso_.operacao, Operacao_.nome);
repository.from(Aplicacao.class).where(p).list();
```
O método EntityManager#flush é invocado automaticamente ao executar os métodos remove, save e update.


Podem selecionar também dados específicos de modelos diferentes:
```java
public class NomeRecursoENomeAplicacao {
	public String descricao;
	public String nome;

	public NomeRecursoENomeAplicacao(String descricao, String nome) {
		this.descricao = descricao;
		this.nome = nome;
	}
}

List<NomeRecursoENomeAplicacao> objects = repository
		.from(Aplicacao.class)
		.select(Aplicacao_.descricao)
		.select(Aplicacao_.recursos, Recurso_.nome)
		.as(NomeRecursoENomeAplicacao.class)
		.where(1, Comparators.GREATER_THAN_OR_EQUAL, Aplicacao_.id)
		.list();
```


Para uso de tenant pode ser integrado ao projeto https://github.com/clairton/repository-tenant e https://github.com/clairton/tenant.


Para usar será necessário adicionar os repositórios maven:

```xml
<repository>
	<id>mvn-repo-releases</id>
	<url>https://raw.github.com/clairton/mvn-repo/releases</url>
</repository>
<repository>
	<id>mvn-repo-snapshot</id>
	<url>https://raw.github.com/clairton/mvn-repo/snapshots</url>
</repository>
```
 Também adicionar as depêndencias:
```xml
<dependency>
    <groupId>br.eti.clairton</groupId>
	<artifactId>repository</artifactId>
	<version>${latestVersion}</version>
</dependency>
```
