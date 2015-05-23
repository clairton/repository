package br.eti.clairton.repository;

import java.util.Collection;

public interface PaginatedCollection<T, X> extends Collection<T>{
	
	public <W> W unwrap(Class<W> type);
}
