package com.beansgalaxy.backpacks.data.config.types;

import com.beansgalaxy.backpacks.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

public class CodecConfigVariant<T> extends ConfigVariant<T> {

      private final Codec<T> codec;

      protected CodecConfigVariant(String name, Codec<T> codec, T defau, String comment) {
            super(name, defau, comment);
            this.codec = codec;
      }

      @Override
      public String encode() {
            return toString() + codec.encodeStart(JsonOps.INSTANCE, value);
      }

      @Override
      public void decode(JsonObject jsonObject) {
            JsonElement jsonElement = jsonObject.get(name);
            DataResult<Pair<T, JsonElement>> result = codec.decode(JsonOps.INSTANCE, jsonElement);

            result.ifError(pairError ->
                        Constants.LOG.warn("Error while parsing beansbackpacks config at \"{}\"; Message: {}", name, pairError.message())
            );

            if (result.isSuccess()) {
                  Pair<T, JsonElement> pair = result.getOrThrow();
                  value = pair.getFirst();
            }
      }
}
