package com.energyxxer.xswing;

import com.energyxxer.util.Confirmation;

public class TemporaryConfirmation implements Confirmation {
    private long end;

    public TemporaryConfirmation() {
        this(3);
    }

    public TemporaryConfirmation(float seconds) {
        this.end = System.currentTimeMillis() + (long) (1000*seconds);
    }

    @Override
    public boolean confirm() {
        return System.currentTimeMillis() < end;
    }
}
