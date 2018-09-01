package br.eti.clairton.repository;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import br.eti.clairton.model.Model_;

@StaticMetamodel(Aplicacao.class)
public abstract class Aplicacao_ extends Model_ {

	public static volatile CollectionAttribute<Aplicacao, Recurso> recursos;
	public static volatile SingularAttribute<Aplicacao, String> nome;
	public static volatile SingularAttribute<Aplicacao, String> descricao;

}
