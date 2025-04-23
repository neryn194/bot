package com.neryn.norman.entity.clan.estate;

public interface ClanEstateLevel<ImplClass extends ClanEstateLevel<ImplClass>> {
    int getLevel();
    int getUpCoins();
    int getUpDiamonds();
    int getUpHours();
    int getUpClanLevel();
    int getExperienceFromWork();
    ImplClass getNext();

    ImplClass GET_MAX_LEVEL();
}
