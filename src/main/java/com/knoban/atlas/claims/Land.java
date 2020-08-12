package com.knoban.atlas.claims;

import java.util.HashMap;

/**
 * Think of this Land class as the keeper of world-specific Estate data. There's absolutely nothing you
 * should need to do in this class.
 * @author Alden Bansemer (kNoAPP)
 */
public class Land {

    protected HashMap<Long, Estate> allEstates = new HashMap<>();

    protected Land() {}
}
