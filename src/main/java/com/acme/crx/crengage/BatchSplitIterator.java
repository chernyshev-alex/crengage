package com.acme.crx.crengage;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Custom batch splitter
 */
public class BatchSplitIterator<T> implements Spliterator<List<T>> {
    
    private final Spliterator<T> parent;
    private final int batchSize;
    
    public BatchSplitIterator(Spliterator<T> parent, int batchSize) {
        this.parent = parent;
        this.batchSize = batchSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<T>> action) {
        final List<T> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize && parent.tryAdvance(batch::add); i++) {
            ;
        }
        if (batch.isEmpty()) return false;
        action.accept(batch);
        return true;
    }

    @Override
    public Spliterator<List<T>> trySplit() {
        if (parent.estimateSize() <= batchSize) {
            return null;
        }
        final Spliterator<T> splitParent = this.parent.trySplit();
        return splitParent == null ? null : new BatchSplitIterator<>(splitParent, batchSize);
    }

    @Override
    public long estimateSize() {
        final double baseSize = parent.estimateSize();
        return baseSize == 0 ? 0 : (long) Math.ceil(baseSize / (double) batchSize);
    }

    @Override
    public int characteristics() {
        return parent.characteristics();
    }
    
    public static <T> Stream<List<T>> toBatchStream(Stream<T> source, int batchSize) {
        return StreamSupport.stream(new BatchSplitIterator<>(source.spliterator(), batchSize), false);
    }
    
}
