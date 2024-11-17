package com.hf.webflux.hfai.cex.strategy;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;

import org.ta4j.core.num.Num;
import java.util.function.BiFunction;

public class DualTransformIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> first;
    private final Indicator<Num> second;
    private final BiFunction<Num, Num, Num> transformationFunction;

    /**
     * Constructor.
     *
     * @param first                 The first indicator
     * @param second                The second indicator
     * @param transformationFunction A BiFunction describing the transformation
     */
    public DualTransformIndicator(Indicator<Num> first, Indicator<Num> second,
                                  BiFunction<Num, Num, Num> transformationFunction) {
        super(first);
        this.first = first;
        this.second = second;
        this.transformationFunction = transformationFunction;
    }

    @Override
    protected Num calculate(int index) {
        return transformationFunction.apply(first.getValue(index), second.getValue(index));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public int getUnstableBars() {
        return 0;
    }
}
