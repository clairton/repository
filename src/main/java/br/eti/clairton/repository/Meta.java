package br.eti.clairton.repository;

import java.io.Serializable;

public class Meta implements Serializable {
	private static final long serialVersionUID = -4584892874560552561L;
	private final Long total;
	private final Long page;

	public Meta(Long total, Long page) {
		super();
		this.total = total;
		this.page = page;
	}

	public Long getPage() {
		return page;
	}

	public Long getTotal() {
		return total;
	}
}
