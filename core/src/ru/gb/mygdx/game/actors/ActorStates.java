package ru.gb.mygdx.game.actors;

public enum ActorStates {
     AS_STANDING        (true)
    ,AS_RUNNING         (true)
    ,AS_CLIMBING        (true)
    ,AS_JUMPING_UP      (false)
    ,AS_JUMPING_FORTH   (false)
    ;//                  idle

    public final boolean isIdleStateApplicable;

    ActorStates (boolean idleStateApplicable) {
        isIdleStateApplicable = idleStateApplicable;
    }
}
