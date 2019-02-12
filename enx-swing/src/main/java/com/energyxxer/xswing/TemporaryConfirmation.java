package com.energyxxer.xswing;

import com.energyxxer.util.Confirmation;

public class TemporaryConfirmation implements Confirmation {
    private int confirmations = 0;

    @Override
    public boolean confirm() {
        confirmations++;
        return confirmations < 200;
    }
}
