package br.eti.clairton.repository;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class TenantValueObject implements TenantValue<String> {

	@Override
	public String get() {
		return "OutroTesteQueNãoDeveAparecerNaConsulta";
	}

}
