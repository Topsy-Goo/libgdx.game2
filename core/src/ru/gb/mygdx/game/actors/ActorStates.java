package ru.gb.mygdx.game.actors;

public enum ActorStates {
     AS_STANDING        (true,  false, true )
    ,AS_RUNNING         (true,  false, true )
    ,AS_CLIMBING        (true,  false, true )
    ,AS_JUMPING_UP      (false, true,  false)
    ,AS_JUMPING_FORTH   (false, true,  false)
    ,AS_FALLING         (false, true,  false)
    ;//                  cnidl   jmp   cnfal

    public final boolean
        canTurnToIdle, //< состояние допускает переход в состояние бездействия
        isJumping,     //< какое-либо состояние прыжка.
        canTurnToFall; //< состояние допускает переход в состояние падения.

    ActorStates (boolean canidle, boolean isjumping, boolean canfall)
    {
        canTurnToIdle = canidle;
        isJumping     = isjumping;
        canTurnToFall = canfall;
    }
}
