package com.beansgalaxy.backpacks.traits.experience;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

class XpPackagable {
      int totalExperience = 0;
      int experienceLevel = 0;
      float experienceProgress = 0f;

      XpPackagable(int points) {
            totalExperience = points;
            int levels = 0;
            int forNextLevel = getXpNeededForNextLevel(levels);
            while (points > forNextLevel) {
                  points -= forNextLevel;

                  levels++;
                  forNextLevel = getXpNeededForNextLevel(levels);
            }

            if (points == forNextLevel)
                  levels++;
            else
                  experienceProgress = (float) points / forNextLevel;

            experienceLevel = levels;
      }

      XpPackagable(int levels, float progress) {
            experienceLevel = levels;
            experienceProgress = progress;
            for (int i = 0; i < levels; i++) {
                  totalExperience += getXpNeededForNextLevel(i);
            }

            int forNextLevel = getXpNeededForNextLevel(levels);
            totalExperience += Mth.ceil(forNextLevel * progress);
      }

      XpPackagable(Player player) {
            totalExperience = player.totalExperience;
            experienceLevel = player.experienceLevel;
            experienceProgress = player.experienceProgress;
      }

      static int getXpNeededForNextLevel(int experienceLevel) {
            if (experienceLevel >= 30)
                  return 112 + (experienceLevel - 30) * 9;

            if (experienceLevel >= 15)
                  return 37 + (experienceLevel - 15) * 5;

            return  7 + experienceLevel * 2;
      }

      public XpPackagable applyTo(Player player) {
            player.totalExperience = totalExperience;
            player.experienceLevel = experienceLevel;
            player.experienceProgress = experienceProgress;
            return this;
      }
}
