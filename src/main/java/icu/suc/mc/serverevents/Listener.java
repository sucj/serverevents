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
package icu.suc.mc.serverevents;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a listener for specific events in the application.
 * <p>
 * This interface is used to define callbacks that can be triggered when certain events occur.
 * Listeners must implement the methods defined in this interface to specify the events they are interested in
 * and the actions to be taken when those events are triggered.
 * <p>
 * Listeners can specify a phase using the `phase()` method. The default phase is [Event#DEFAULT_PHASE].
 * <p>
 * The `events()` method must return an array of event types that the listener is interested in.
 * When an event of one of these types occurs, the corresponding method in the listener will be invoked.
 */
public interface Listener {
    /**
     * Returns the phase in which the listener is interested.
     *
     * @return the default phase [Event.DEFAULT_PHASE]
     */
    default @NotNull Identifier phase() {
        return Event.DEFAULT_PHASE;
    }

    /**
     * Returns an array of event types that the listener is interested in.
     * <p>
     * When an event of one of these types occurs, the corresponding method in the listener will be invoked.
     *
     * @return An array of event types.
     */
    @NotNull Event<?>[] events();
}
