package br.eti.clairton.repository;

import java.util.ArrayList;
import java.util.Collection;

public class PaginatedMetaList<T> extends ArrayList<T> implements PaginatedList<T, Meta> {
	private static final long serialVersionUID = 1L;

	private final Meta meta;

	public PaginatedMetaList(final Collection<? extends T> c, final Meta meta) {
		super(c);
		this.meta = meta;
	}
	
	public Meta getMeta() {
		return meta;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <W> W unwrap(Class<W> type) {
		return (W) meta;
	}	
}
