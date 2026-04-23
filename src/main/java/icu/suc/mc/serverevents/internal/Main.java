/*
 * MIT License
 *
 * Copyright (c) 2025 sucj
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package icu.suc.mc.serverevents.internal;

import icu.suc.mc.serverevents.ServerEventPriority;
import icu.suc.mc.serverevents.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

public final class Main implements ModInitializer {
    private static void addPhaseOrdering(@NotNull Class<?> clazz, Identifier[] priorities) {
        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            if (!Event.class.isAssignableFrom(field.getType())) continue;

            try {
                var event = (Event<?>) field.get(null);

                if (event == null) continue;

                for (int i = 0; i < priorities.length - 1; i++) {
                    event.addPhaseOrdering(priorities[i], priorities[i + 1]);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (var nested : clazz.getDeclaredClasses()) {
            if (!Modifier.isStatic(nested.getModifiers())) continue;

            addPhaseOrdering(nested, priorities);
        }
    }

    @Override
    public void onInitialize() {
        if (System.getProperty("serverevents.disableeventpriority") == null) {
            addPhaseOrdering(ServerEvents.class, ServerEventPriority.PRIORITIES);
        }
    }
}
