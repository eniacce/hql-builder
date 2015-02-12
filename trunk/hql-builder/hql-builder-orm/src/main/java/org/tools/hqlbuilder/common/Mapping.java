package org.tools.hqlbuilder.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mapping<S, T> {
    protected static final Logger logger = LoggerFactory.getLogger(Mapping.class);

    protected final ClassPair<S, T> classPair;

    protected final Map<String, Property<Object, Object>> sourceInfo = new HashMap<>();

    protected final Map<String, Property<Object, Object>> targetInfo = new HashMap<>();

    protected final List<Mapper<S, T>> mappers = new ArrayList<>();

    protected final List<String> conditionals = new ArrayList<String>();

    protected final List<String> collections = new ArrayList<String>();

    public Mapping(ClassPair<S, T> classPair) {
        this.classPair = classPair;
    }

    public Mapping<S, T> add(Mapper<S, T> consumer) {
        this.mappers.add(consumer);
        return this;
    }

    public Mapping<S, T> clear() {
        this.collections.clear();
        this.conditionals.clear();
        this.mappers.clear();
        return this;
    }

    /**
     * TODO sourceProperty
     */
    public <SC, TC> Mapping<S, T> collect(MappingFactory factory, String sourceProperty, Class<TC> targetType) {
        return collect(factory, sourceProperty, sourceProperty, targetType);
    }

    /**
     * TODO sourceProperty,targetProperty
     */
    public <SC, TC> Mapping<S, T> collect(MappingFactory factory, String sourceProperty, String targetProperty, Class<TC> targetType) {
        this.mappers.add(new Mapper<S, T>() {
            @Override
            public void apply(Map<Object, Object> context, S source, T target) {
                @SuppressWarnings("unchecked")
                Property<S, Object> sourcePD = (Property<S, Object>) Mapping.this.sourceInfo.get(sourceProperty);
                @SuppressWarnings("unchecked")
                Property<T, Object> targetPD = (Property<T, Object>) Mapping.this.targetInfo.get(targetProperty);
                Object sourceIterable = sourcePD.read(source);
                Collection<TC> tmpTargetCollection = new ArrayList<TC>();
                if (sourceIterable instanceof Collection) {
                    for (Object sourceIt : Collection.class.cast(sourceIterable)) {
                        convertAndAdd(context, tmpTargetCollection, sourceIt);
                    }
                } else {
                    for (Object sourceIt : Object[].class.cast(sourceIterable)) {
                        convertAndAdd(context, tmpTargetCollection, sourceIt);
                    }
                }

                Object targetIterable;
                if (Object[].class.isAssignableFrom(targetPD.type())) {
                    targetIterable = Object[].class.cast(Array.newInstance(targetPD.type().getComponentType(), tmpTargetCollection.size()));
                    Iterator<TC> iterator = tmpTargetCollection.iterator();
                    for (int i = 0; i < tmpTargetCollection.size(); i++) {
                        Array.set(targetIterable, i, iterator.next());
                    }
                } else {
                    targetIterable = targetPD.read(target);
                    if (targetIterable == null) {
                        try {
                            targetIterable = targetPD.type().newInstance();
                        } catch (InstantiationException | IllegalAccessException | RuntimeException ex1) {
                            //
                        }
                    }
                    if (targetIterable == null) {
                        if (SortedSet.class.isAssignableFrom(targetPD.type())) {
                            targetIterable = new TreeSet<TC>(tmpTargetCollection);
                        } else if (Set.class.isAssignableFrom(targetPD.type())) {
                            targetIterable = new HashSet<TC>(tmpTargetCollection);
                        } else if (List.class.isAssignableFrom(targetPD.type())) {
                            targetIterable = new ArrayList<TC>(tmpTargetCollection);
                        } else {
                            targetIterable = new ArrayList<TC>(tmpTargetCollection);
                        }
                    } else {
                        @SuppressWarnings("unchecked")
                        Collection<TC> coll = Collection.class.cast(targetIterable);
                        coll.addAll(tmpTargetCollection);
                    }
                }
                targetPD.write(target, targetIterable);
            }

            protected void convertAndAdd(Map<Object, Object> context, Collection<TC> tmpTargetCollection, Object obj) {
                @SuppressWarnings("unchecked")
                SC sourceIt = (SC) obj;
                TC targetIt;
                if (context.containsKey(sourceIt)) {
                    targetIt = targetType.cast(context.get(sourceIt));
                } else {
                    targetIt = factory.map(context, sourceIt, targetType);
                }
                tmpTargetCollection.add(targetIt);
            }
        });
        return this;
    }

    protected void collections(String property) {
        this.collections.add(property);
    }

    protected void conditional(String property) {
        this.conditionals.add(property);
    }

    /**
     * debug info
     */
    public void debug() {
        //
    }

    protected Map<String, Property<Object, Object>> getSourceInfo() {
        return this.sourceInfo;
    }

    protected Map<String, Property<Object, Object>> getTargetInfo() {
        return this.targetInfo;
    }

    protected T map(Map<Object, Object> context, MappingFactory factory, S source) throws MappingException {
        try {
            T target = this.classPair.getTargetClass().newInstance();
            return this.map(context, factory, source, target);
        } catch (InstantiationException | IllegalAccessException ex1) {
            throw new MappingException(ex1);
        }
    }

    protected T map(Map<Object, Object> context, MappingFactory factory, S source, T target) throws MappingException {
        try {
            Mapping.logger.trace("add to context: " + context.size() + ": " + System.identityHashCode(source) + ": " + source + " > " + target);
            if (context.containsKey(source)) {
                throw new RuntimeException();
            }
            context.put(source, target);
            T proxy = null;
            for (Mapper<S, T> consumer : this.mappers) {
                try {
                    try {
                        consumer.apply(context, source, target);
                    } catch (NullPointerException ex) {
                        Mapping.logger.trace("proxy for " + target);
                        if (proxy == null) {
                            proxy = this.proxy(target);
                        }
                        consumer.apply(context, source, proxy);
                    }
                } catch (RuntimeException ex) {
                    throw new MappingException(ex);
                }
            }
            for (String conditional : this.conditionals) {
                Property<Object, Object> nestedSourcePD = this.sourceInfo.get(conditional);
                Property<Object, Object> nestedTargetPD = this.targetInfo.get(conditional);
                Class<?> nestedSourceClass = nestedSourcePD.type();
                Class<?> nestedTargetClass = nestedTargetPD.type();
                if (!factory.accept(nestedSourceClass, nestedTargetClass)) {
                    continue;
                }
                Object nestedSourceValue = nestedSourcePD.read(source);
                if (nestedSourceValue == null) {
                    nestedTargetPD.write(target, null);
                    continue;
                }
                boolean breaker = false;
                for (Entry<Object, Object> entry : context.entrySet()) {
                    if (entry.getKey() == nestedSourceValue) {
                        Mapping.logger.trace("reusing " + nestedSourceValue + " > " + entry.getValue());
                        nestedTargetPD.write(target, entry.getValue());
                        breaker = true;
                        break;
                    }
                }
                if (breaker) {
                    continue;
                }
                Object nestedTargetValue = nestedTargetPD.read(target);
                if (nestedTargetValue == null) {
                    nestedTargetValue = nestedTargetClass.newInstance();
                    nestedTargetPD.write(target, nestedTargetValue);
                }
                factory.map(context, nestedSourceValue, nestedTargetValue);
            }
            return target;
        } catch (InstantiationException | IllegalAccessException ex1) {
            throw new MappingException(ex1);
        }
    }

    /**
     * map <S> to new <T>
     */
    public T map(MappingFactory factory, S source) throws MappingException {
        return this.map(new HashMap<>(), factory, source);
    }

    /**
     * map <S> to existing <T>
     */
    public T map(MappingFactory factory, S source, T target) throws MappingException {
        return this.map(new HashMap<>(), factory, source, target);
    }

    protected T proxy(final T target) throws InstantiationException, IllegalAccessException {
        ProxyFactory f = new ProxyFactory();
        f.setSuperclass(this.classPair.getTargetClass());
        MethodHandler mi = new MethodHandler() {
            @Override
            public Object invoke(Object self, java.lang.reflect.Method method, java.lang.reflect.Method paramMethod2, Object[] args) throws Throwable {
                try {
                    Object invoke = method.invoke(target, args);
                    if (method.getName().startsWith("get") && (args.length == 0) && (invoke == null)) {
                        String propertyName = method.getName().substring(3);
                        propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                        Property<Object, Object> pd = Mapping.this.targetInfo.get(propertyName);
                        invoke = method.getReturnType().newInstance();
                        pd.write(target, invoke);
                    }
                    return invoke;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        };
        @SuppressWarnings("unchecked")
        T proxy = (T) f.createClass().newInstance();
        ((ProxyObject) proxy).setHandler(mi);
        return proxy;
    }

    protected void setSourceInfo(Map<String, Property<Object, Object>> sourceInfo) {
        this.sourceInfo.clear();
        this.sourceInfo.putAll(sourceInfo);
    }

    protected void setTargetInfo(Map<String, Property<Object, Object>> targetInfo) {
        this.targetInfo.clear();
        this.targetInfo.putAll(targetInfo);
    }
}