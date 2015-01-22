package br.eti.clairton.repository;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;

import net.vidageek.mirror.dsl.Mirror;

import org.jboss.weld.context.BoundContext;
import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.context.beanstore.MapBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;

public class CdiJUnit4Runner extends BlockJUnit4ClassRunner {
	private final static Mirror MIRROR = new Mirror();
	private final Class<?> klass;
	private final Weld weld;
	private final WeldContainer container;
	private static final ThreadLocal<MapBeanStore> beanStore = new ThreadLocal<MapBeanStore>() {
		public MapBeanStore get() {
			final NamingScheme namingScheme = new SimpleNamingScheme("");
			final Map<String, Object> delegate = new HashMap<>();
			return new MapBeanStore(namingScheme, delegate);
		};
	};

	public CdiJUnit4Runner(final Class<?> klass)
			throws org.junit.runners.model.InitializationError {
		super(klass);
		this.klass = klass;
		this.weld = new Weld();
		this.container = weld.initialize();
	}

	@Override
	protected Object createTest() throws Exception {
		activate(BoundRequestContext.class);
		final Object test = container.instance().select(klass).get();
		return test;
	}

	private <T extends ManagedContext & BoundContext<Map<String, Object>>> void activate(
			Class<T> klazz) {
		final Instance<Object> instance = container.instance();
		final T scope = instance.select(klazz).get();
		MIRROR.on(scope).set().field("beanStore").withValue(beanStore);
		scope.activate();
	}
}