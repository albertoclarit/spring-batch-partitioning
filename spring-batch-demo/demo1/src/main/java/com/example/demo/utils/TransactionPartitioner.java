package com.example.demo.utils;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TransactionPartitioner implements Partitioner {
    private static final String PARTITION_KEY = "partition";
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);

        IntStream.range(0,5).forEach((i)->{
            ExecutionContext context = new ExecutionContext();
            context.putString("partition_index", i + "");
            map.put(PARTITION_KEY + (i+ 1), context);
        });
        return map;
    }
}
