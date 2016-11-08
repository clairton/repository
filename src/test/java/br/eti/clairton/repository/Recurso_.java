package br.eti.clairton.repository;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import br.eti.clairton.model.Model_;

@StaticMetamodel(Recurso.class)
public abstract class Recurso_ extends Model_ {

	public static volatile SingularAttribute<Recurso, Aplicacao> aplicacao;
	public static volatile SingularAttribute<Recurso, String> nome;
	public static volatile CollectionAttribute<Recurso, Operacao> operacoes;

}
