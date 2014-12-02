package br.eti.clairton.repository;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Recurso.class)
public abstract class Recurso_ extends br.eti.clairton.repository.Model_ {

	public static volatile SingularAttribute<Recurso, Aplicacao> aplicacao;
	public static volatile SingularAttribute<Recurso, String> nome;
	public static volatile CollectionAttribute<Recurso, Operacao> operacoes;

}

