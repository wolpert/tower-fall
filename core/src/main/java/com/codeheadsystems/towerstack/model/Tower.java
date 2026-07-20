package com.codeheadsystems.towerstack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The stack of blocks that have been successfully placed, bottom to top. The last block
 * is the current top — the footprint the next dropped block is sliced against.
 */
public class Tower {

    private final List<Block> blocks = new ArrayList<>();

    public void add(Block block) {
        blocks.add(block);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public Block top() {
        if (blocks.isEmpty()) {
            throw new IllegalStateException("Tower is empty; there is no top block.");
        }
        return blocks.get(blocks.size() - 1);
    }

    /** Number of blocks in the tower, including the starting base block. */
    public int size() {
        return blocks.size();
    }

    /** Read-only view for rendering. */
    public List<Block> blocks() {
        return Collections.unmodifiableList(blocks);
    }

    public void clear() {
        blocks.clear();
    }
}
