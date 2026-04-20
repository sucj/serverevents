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

import net.fabricmc.fabric.api.entity.event.v1.*;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServerEvents {
    private ServerEvents() {
    }

    /**
     * The identifier of this mod.
     */
    public static final String ID = "serverevents";

    /**
     * Registers the given listener.
     *
     * @see ServerEvents#register(Identifier, Object, Event[])
     * @param listener the listener to register
     */
    public static void register(@NotNull Listener listener) {
        register(listener.phase(), listener, listener.events());
    }

    /**
     * Registers the given listener to one or more events using the default phase.
     *
     * <p>The {@code listener} object must implement the interface or class represented
     * by each {@code event}'s type parameter {@code T}. Passing an incompatible object
     * will throw a {@link ClassCastException} at runtime.</p>
     *
     * <p>This variant uses {@link Event#DEFAULT_PHASE} as the
     * registration phase.</p>
     *
     * @param listener the object to register; must implement each event's type parameter {@code T}
     * @param events the events to register to
     * @param <T> the type of the event listener; each {@code event} expects this type
     */
    @SafeVarargs
    public static <T> void register(@NotNull Object listener, Event<? extends T> @NotNull ... events) {
        register(Event.DEFAULT_PHASE, listener, events);
    }

    /**
     * Registers the given listener to one or more events with a specified phase.
     *
     * <p>The {@code listener} object must implement the interface or class represented
     * by each {@code event}'s type parameter {@code T}. Passing an incompatible object
     * will throw a {@link ClassCastException} at runtime.</p>
     *
     * @param phase the registration phase to use
     * @param listener the object to register; must implement each event's type parameter {@code T}
     * @param events the events to register to
     * @param <T> the type of the event listener; each {@code event} expects this type
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> void register(@NotNull Identifier phase, @NotNull Object listener, Event<? extends T> @NotNull ... events) {
        for (Event<? extends T> event : events) {
            ((Event<T>) event).register(phase, (T) listener);
        }
    }

    public static final class Player {
        private Player() {}

        /**
         * @see ServerPlayerEvents#COPY_FROM
         */
        @FabricAPI public static final Event<ServerPlayerEvents.CopyFrom> COPY_FROM = ServerPlayerEvents.COPY_FROM;

        /**
         * @see ServerPlayerEvents#AFTER_RESPAWN
         */
        @FabricAPI public static final Event<ServerPlayerEvents.AfterRespawn> AFTER_RESPAWN = ServerPlayerEvents.AFTER_RESPAWN;

        /**
         * An event that can be used to provide the player's join message.
         *
         * @deprecated Moved to {@link ServerEvents.Player.Join#MODIFY_MESSAGE}.
         * @since 1.0.5
         */
        @Deprecated
        public static final Event<ServerEvents.Player.ModifyJoinMessage> MODIFY_JOIN_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.ModifyJoinMessage.class, callbacks -> (player, message) -> {
            for (ServerEvents.Player.ModifyJoinMessage callback : callbacks) {
                message = callback.modifyJoinMessage(player, message);
            }
            return message;
        });

        /**
         * An event that can be used to provide the player's leave message.
         *
         * @deprecated Moved to {@link ServerEvents.Player.Leave#MODIFY_MESSAGE}.
         * @since 1.0.5
         */
        @Deprecated
        public static final Event<ServerEvents.Player.ModifyLeaveMessage> MODIFY_LEAVE_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.ModifyLeaveMessage.class, callbacks -> (player, message) -> {
            for (ServerEvents.Player.ModifyLeaveMessage callback : callbacks) {
                message = callback.modifyLeaveMessage(player, message);
            }
            return message;
        });

        /**
         * An event that allows the player to drop the selected item.
         */
        public static final Event<ServerEvents.Player.AllowDropSelectedItem> ALLOW_DROP_SELECTED_ITEM = EventFactory.createArrayBacked(ServerEvents.Player.AllowDropSelectedItem.class, callbacks -> (player, all) -> {
            for (ServerEvents.Player.AllowDropSelectedItem callback : callbacks) {
                if (!callback.allowDropSelectedItem(player, all)) {
                    return false;
                }
            }
            return true;
        });

        /**
         * @deprecated Moved to {@link ServerEvents.Player.Join.ModifyMessage}.
         * @since 1.0.5
         */
        @Deprecated
        @FunctionalInterface
        public interface ModifyJoinMessage {
            /**
             * Modifies or provides a message for a player joined.
             *
             * @param player the player
             * @param message the join message
             * @return the new join message
             */
            @NotNull Component modifyJoinMessage(@NotNull ServerPlayer player, @NotNull Component message);
        }

        /**
         * @deprecated Moved to {@link ServerEvents.Player.Leave.ModifyMessage}.
         * @since 1.0.5
         */
        @Deprecated
        @FunctionalInterface
        public interface ModifyLeaveMessage {
            /**
             * Modifies or provides a message for a player left.
             *
             * @param player the player
             * @param message the leaving message
             * @return the new leave message
             */
            @NotNull Component modifyLeaveMessage(@NotNull ServerPlayer player, @NotNull Component message);
        }

        @FunctionalInterface
        public interface AllowDropSelectedItem {
            /**
             * Called when the player drops the selected item.
             *
             * @param player the player
             * @param all whether to drop all items
             * @return {@code true} if should be allowed, otherwise {@code false}
             */
            boolean allowDropSelectedItem(@NotNull ServerPlayer player, boolean all);
        }

        public static final class Join {
            private Join() {}

            /**
             * An event that can be used to provide the player's join message.
             */
            public static final Event<ServerEvents.Player.Join.ModifyMessage> MODIFY_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.Join.ModifyMessage.class, callbacks -> (player, message) -> {
                for (ServerEvents.Player.Join.ModifyMessage callback : callbacks) {
                    message = callback.modifyJoinMessage(player, message);
                }
                return message;
            });

            /**
             * An event that allows broadcast the player's join message.
             */
            public static final Event<ServerEvents.Player.Join.AllowMessage> ALLOW_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.Join.AllowMessage.class, callbacks -> (player, message) -> {
                for (ServerEvents.Player.Join.AllowMessage callback : callbacks) {
                    if (!callback.allowJoinMessage(player, message)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface ModifyMessage {
                /**
                 * Modifies or provides a message for a player joined.
                 *
                 * @param player the player
                 * @param message the join message
                 * @return the new join message
                 */
                @NotNull Component modifyJoinMessage(@NotNull ServerPlayer player, @NotNull Component message);
            }

            @FunctionalInterface
            public interface AllowMessage {
                /**
                 * Called when a player joins a server
                 *
                 * @param player the player
                 * @param message the join reason
                 * @return {@code true} if should broadcast the message, otherwise {@code false}
                 */
                boolean allowJoinMessage(@NotNull ServerPlayer player, @NotNull Component message);
            }
        }

        public static final class Leave {
            private Leave() {}

            /**
             * An event that can be used to provide the player's leave message.
             */
            public static final Event<ServerEvents.Player.Leave.ModifyMessage> MODIFY_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.Leave.ModifyMessage.class, callbacks -> (player, message) -> {
                for (ServerEvents.Player.Leave.ModifyMessage callback : callbacks) {
                    message = callback.modifyLeaveMessage(player, message);
                }
                return message;
            });

            /**
             * An event that allows broadcast the player's leave message.
             */
            public static final Event<ServerEvents.Player.Leave.AllowMessage> ALLOW_MESSAGE = EventFactory.createArrayBacked(ServerEvents.Player.Leave.AllowMessage.class, callbacks -> (player, message) -> {
                for (ServerEvents.Player.Leave.AllowMessage callback : callbacks) {
                    if (!callback.allowLeaveMessage(player, message)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface ModifyMessage {
                /**
                 * Modifies or provides a message for a player left.
                 *
                 * @param player the player
                 * @param message the leaving message
                 * @return the new leave message
                 */
                @NotNull Component modifyLeaveMessage(@NotNull ServerPlayer player, @NotNull Component message);
            }

            @FunctionalInterface
            public interface AllowMessage {
                /**
                 * Called when a player left a server
                 *
                 * @param player the player
                 * @param message the leave reason
                 * @return {@code true} if should broadcast the message, otherwise {@code false}
                 */
                boolean allowLeaveMessage(@NotNull ServerPlayer player, @NotNull Component message);
            }
        }

        public static final class Kick {
            private Kick() {}

            /**
             * An event that can be used to provide the player's kick reason.
             */
            public static final Event<ServerEvents.Player.Kick.ModifyReason> MODIFY_REASON = EventFactory.createArrayBacked(ServerEvents.Player.Kick.ModifyReason.class, callbacks -> (player, message) -> {
                for (ServerEvents.Player.Kick.ModifyReason callback : callbacks) {
                    message = callback.modifyKickReason(player, message);
                }
                return message;
            });

            /**
             * An event that allows the player should be kicked.
             */
            public static final Event<ServerEvents.Player.Kick.Allow> ALLOW = EventFactory.createArrayBacked(ServerEvents.Player.Kick.Allow.class, callbacks -> (player, reason) -> {
                for (ServerEvents.Player.Kick.Allow callback : callbacks) {
                    if (!callback.allowKick(player, reason)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface ModifyReason {
                /**
                 * Modifies or provides a reason for a player kicked.
                 *
                 * @param player the player
                 * @param reason the kick reason
                 * @return the new kick reason
                 */
                @NotNull Component modifyKickReason(@NotNull ServerPlayer player, @NotNull Component reason);
            }

            @FunctionalInterface
            public interface Allow {
                /**
                 * Called when a player kicks a server
                 *
                 * @param player the player
                 * @param reason the kick reason
                 * @return {@code true} if the player should be kicked, otherwise {@code false}
                 */
                boolean allowKick(@NotNull ServerPlayer player, @NotNull Component reason);
            }
        }

        public static final class Interact {
            private Interact() {}

            public static final class Use {
                private Use() {}

                /**
                 * @see UseBlockCallback#EVENT
                 */
                @FabricAPI public static final Event<UseBlockCallback> BLOCK = UseBlockCallback.EVENT;

                /**
                 * @see UseEntityCallback#EVENT
                 */
                @FabricAPI public static final Event<UseEntityCallback> ENTITY = UseEntityCallback.EVENT;

                /**
                 * @see UseItemCallback#EVENT
                 */
                @FabricAPI public static final Event<UseItemCallback> ITEM = UseItemCallback.EVENT;
            }

            public static final class Attack {
                private Attack() {}

                /**
                 * @see AttackBlockCallback#EVENT
                 */
                @FabricAPI public static final Event<AttackBlockCallback> BLOCK = AttackBlockCallback.EVENT;

                /**
                 * @see AttackEntityCallback#EVENT
                 */
                @FabricAPI public static final Event<AttackEntityCallback> ENTITY = AttackEntityCallback.EVENT;
            }

            public static final class Break {
                private Break() {}

                /**
                 * @see PlayerBlockBreakEvents#BEFORE
                 */
                @FabricAPI public static final Event<PlayerBlockBreakEvents.Before> BEFORE = PlayerBlockBreakEvents.BEFORE;

                /**
                 * @see PlayerBlockBreakEvents#AFTER
                 */
                @FabricAPI public static final Event<PlayerBlockBreakEvents.After> AFTER = PlayerBlockBreakEvents.AFTER;

                /**
                 * @see PlayerBlockBreakEvents#CANCELED
                 */
                @FabricAPI public static final Event<PlayerBlockBreakEvents.Canceled> CANCELED = PlayerBlockBreakEvents.CANCELED;
            }

            public static final class Pick {
                private Pick() {}

                /**
                 * @see PlayerPickItemEvents#BLOCK
                 */
                @FabricAPI public static final Event<PlayerPickItemEvents.PickItemFromBlock> BLOCK = PlayerPickItemEvents.BLOCK;

                /**
                 * @see PlayerPickItemEvents#ENTITY
                 */
                @FabricAPI public static final Event<PlayerPickItemEvents.PickItemFromEntity> ENTITY = PlayerPickItemEvents.ENTITY;
            }
        }
    }

    public static final class Item {
        private Item() {}

        /**
         * @see DefaultItemComponentEvents#MODIFY
         */
        @FabricAPI public static final Event<DefaultItemComponentEvents.ModifyCallback> DEFAULT_COMPONENT_MODIFY = DefaultItemComponentEvents.MODIFY;

        public static final class Enchanting {
            private Enchanting() {}

            /**
             * @see EnchantmentEvents#ALLOW_ENCHANTING
             */
            @FabricAPI public static final Event<EnchantmentEvents.AllowEnchanting> ALLOW = EnchantmentEvents.ALLOW_ENCHANTING;

            /**
             * @see EnchantmentEvents#MODIFY
             */
            @FabricAPI public static final Event<EnchantmentEvents.Modify> MODIFY = EnchantmentEvents.MODIFY;
        }
    }

    public static final class Connection {
        private Connection() {}

        public static final class State {
            private State() {}

            public static final class Configuration {
                private Configuration() {}

                /**
                 * @see ServerConfigurationConnectionEvents#BEFORE_CONFIGURE
                 */
                @FabricAPI public static final Event<ServerConfigurationConnectionEvents.Configure> BEFORE = ServerConfigurationConnectionEvents.BEFORE_CONFIGURE;

                /**
                 * @see ServerConfigurationConnectionEvents#CONFIGURE
                 */
                @FabricAPI public static final Event<ServerConfigurationConnectionEvents.Configure> CONFIGURE = ServerConfigurationConnectionEvents.CONFIGURE;

                /**
                 * @see ServerConfigurationConnectionEvents#DISCONNECT
                 */
                @FabricAPI public static final Event<ServerConfigurationConnectionEvents.Disconnect> DISCONNECT = ServerConfigurationConnectionEvents.DISCONNECT;
            }

            public static final class Login {
                private Login() {}

                /**
                 * @see ServerLoginConnectionEvents#INIT
                 */
                @FabricAPI public static final Event<ServerLoginConnectionEvents.Init> INIT = ServerLoginConnectionEvents.INIT;

                /**
                 * @see ServerLoginConnectionEvents#QUERY_START
                 */
                @FabricAPI public static final Event<ServerLoginConnectionEvents.QueryStart> QUERY_START = ServerLoginConnectionEvents.QUERY_START;

                /**
                 * @see ServerLoginConnectionEvents#DISCONNECT
                 */
                @FabricAPI public static final Event<ServerLoginConnectionEvents.Disconnect> DISCONNECT = ServerLoginConnectionEvents.DISCONNECT;
            }

            public static final class Play {
                private Play() {}

                /**
                 * @see ServerPlayConnectionEvents#INIT
                 */
                @FabricAPI public static final Event<ServerPlayConnectionEvents.Init> INIT = ServerPlayConnectionEvents.INIT;

                /**
                 * @see ServerPlayConnectionEvents#JOIN
                 */
                @FabricAPI public static final Event<ServerPlayConnectionEvents.Join> JOIN = ServerPlayConnectionEvents.JOIN;

                /**
                 * @see ServerPlayConnectionEvents#DISCONNECT
                 */
                @FabricAPI public static final Event<ServerPlayConnectionEvents.Disconnect> DISCONNECT = ServerPlayConnectionEvents.DISCONNECT;
            }
        }

        public static final class Receive {
            private Receive() {}

            /**
             * An event that can be used to modify the packet being received.
             */
            public static final Event<ServerEvents.Connection.Receive.Modify> MODIFY = EventFactory.createArrayBacked(ServerEvents.Connection.Receive.Modify.class, callbacks -> (packetListener, packet) -> {
                for (ServerEvents.Connection.Receive.Modify callback : callbacks) {
                    packet = callback.modifyReceive(packetListener, packet);
                }
                return packet;
            });

            /**
             * An event that determines whether a packet should be received.
             */
            public static final Event<ServerEvents.Connection.Receive.Allow> ALLOW = EventFactory.createArrayBacked(ServerEvents.Connection.Receive.Allow.class, callbacks -> (packetListener, packet) -> {
                for (ServerEvents.Connection.Receive.Allow callback : callbacks) {
                    if (!callback.allowReceive(packetListener, packet)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface Modify {
                /**
                 * Modifies or provides a packet for receiving.
                 *
                 * @param packetListener the packet listener
                 * @param packet the packet being received
                 * @return the modified packet
                 */
                @NotNull Packet<?> modifyReceive(@NotNull PacketListener packetListener, @NotNull Packet<?> packet);
            }

            @FunctionalInterface
            public interface Allow {
                /**
                 * Called when the server receives a packet.
                 *
                 * @param packetListener the packet listener
                 * @param packet the packet being received
                 * @return {@code true} if the packet should be received, otherwise {@code false}
                 */
                boolean allowReceive(@NotNull PacketListener packetListener, @NotNull Packet<?> packet);
            }
        }

        public static final class Send {
            private Send() {}

            /**
             * An event that can be used to modify the packet being sent.
             */
            public static final Event<ServerEvents.Connection.Send.Modify> MODIFY = EventFactory.createArrayBacked(ServerEvents.Connection.Send.Modify.class, callbacks -> (packetListener, packet) -> {
                for (ServerEvents.Connection.Send.Modify callback : callbacks) {
                    packet = callback.modifySend(packetListener, packet);
                }
                return packet;
            });

            /**
             * An event that determines whether a packet should be sent.
             */
            public static final Event<ServerEvents.Connection.Send.Allow> ALLOW = EventFactory.createArrayBacked(ServerEvents.Connection.Send.Allow.class, callbacks -> (packetListener, packet) -> {
                for (ServerEvents.Connection.Send.Allow callback : callbacks) {
                    if (!callback.allowSend(packetListener, packet)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface Modify {
                /**
                 * Modifies or provides a packet for sending.
                 *
                 * @param packetListener the packet listener may be {@code null}
                 * @param packet the packet being sent
                 * @return the modified packet
                 */
                @NotNull Packet<?> modifySend(@Nullable PacketListener packetListener, @NotNull Packet<?> packet);
            }

            @FunctionalInterface
            public interface Allow {
                /**
                 * Called when a packet is sent by the server.
                 *
                 * @param packetListener the packet listener may be {@code null}
                 * @param packet the packet being sent
                 * @return {@code true} if the packet should be sent, otherwise {@code false}
                 */
                boolean allowSend(@Nullable PacketListener packetListener, @NotNull Packet<?> packet);
            }
        }
    }

    public static final class LivingEntity {
        private LivingEntity() {}

        /**
         * @see ServerLivingEntityEvents#MOB_CONVERSION
         */
        @FabricAPI public static final Event<ServerLivingEntityEvents.MobConversion> MOB_CONVERSION = ServerLivingEntityEvents.MOB_CONVERSION;

        public static final class Damage {
            private Damage() {}

            /**
             * @see ServerLivingEntityEvents#ALLOW_DAMAGE
             */
            @FabricAPI public static final Event<ServerLivingEntityEvents.AllowDamage> ALLOW = ServerLivingEntityEvents.ALLOW_DAMAGE;

            /**
             * @see ServerLivingEntityEvents#AFTER_DAMAGE
             */
            @FabricAPI public static final Event<ServerLivingEntityEvents.AfterDamage> AFTER = ServerLivingEntityEvents.AFTER_DAMAGE;
        }

        public static final class Death {
            private Death() {}

            /**
             * @see ServerLivingEntityEvents#ALLOW_DEATH
             */
            @FabricAPI public static final Event<ServerLivingEntityEvents.AllowDeath> ALLOW = ServerLivingEntityEvents.ALLOW_DEATH;

            /**
             * @see ServerLivingEntityEvents#AFTER_DEATH
             */
            @FabricAPI public static final Event<ServerLivingEntityEvents.AfterDeath> AFTER = ServerLivingEntityEvents.AFTER_DEATH;
        }

        public static final class Effect {
            private Effect() {}

            /**
             * An event that determines whether an effect should be applied to an entity.
             */
            public static final Event<ServerEvents.LivingEntity.Effect.Add> ADD = EventFactory.createArrayBacked(ServerEvents.LivingEntity.Effect.Add.class, callbacks -> (affectedEntity, effect, sourceEntity) -> {
                for (ServerEvents.LivingEntity.Effect.Add callback : callbacks) {
                    if (!callback.addEffect(affectedEntity, effect, sourceEntity)) {
                        return false;
                    }
                }
                return true;
            });

            /**
             * An event that allows modification of whether a new effect should override an existing effect.
             */
            public static final Event<ServerEvents.LivingEntity.Effect.Override> OVERRIDE = EventFactory.createArrayBacked(ServerEvents.LivingEntity.Effect.Override.class, callbacks -> (affectedEntity, oldEffect, newEffect, sourceEntity, override) -> {
                for (ServerEvents.LivingEntity.Effect.Override callback : callbacks) {
                    override = callback.overrideEffect(affectedEntity, oldEffect, newEffect, sourceEntity, override);
                }
                return override;
            });

            /**
             * An event that determines whether an effect should be removed from an entity.
             */
            public static final Event<ServerEvents.LivingEntity.Effect.Remove> REMOVE = EventFactory.createArrayBacked(ServerEvents.LivingEntity.Effect.Remove.class, callbacks -> (entity, effect) -> {
                for (ServerEvents.LivingEntity.Effect.Remove callback : callbacks) {
                    if (!callback.removeEffect(entity, effect)) {
                        return false;
                    }
                }
                return true;
            });

            @FunctionalInterface
            public interface Add {
                /**
                 * Called when an effect is about to be applied to an entity.
                 *
                 * @param affectedEntity the entity to which the effect will be applied
                 * @param effect the effect instance to be applied
                 * @param sourceEntity the entity causing the effect, or {@code null} if no source is specified
                 * @return {@code true} if the effect should be applied, otherwise {@code false}
                 */
                boolean addEffect(@NotNull net.minecraft.world.entity.LivingEntity affectedEntity, @NotNull MobEffectInstance effect, @Nullable net.minecraft.world.entity.Entity sourceEntity);
            }

            @FunctionalInterface
            public interface Override {
                /**
                 * Called to determine whether a new effect should override an existing effect.
                 *
                 * @param affectedEntity the entity with the existing effect
                 * @param oldEffect the current effect instance
                 * @param newEffect the new effect instance to apply
                 * @param sourceEntity the entity causing the effect, or {@code null} if no source is specified
                 * @param override whether the effect should be overridden
                 * @return {@code true} if the effect should be overridden, otherwise {@code false}
                 */
                boolean overrideEffect(@NotNull net.minecraft.world.entity.LivingEntity affectedEntity, @NotNull MobEffectInstance oldEffect, @NotNull MobEffectInstance newEffect, @Nullable net.minecraft.world.entity.Entity sourceEntity, boolean override);
            }

            @FunctionalInterface
            public interface Remove {
                /**
                 * Called when an effect is about to be removed from an entity.
                 *
                 * @param entity the entity from which the effect will be removed
                 * @param effect the effect instance to be removed, or {@code null} if no effect is specified
                 * @return {@code true} if the effect should be removed, otherwise {@code false}
                 */
                boolean removeEffect(@NotNull net.minecraft.world.entity.LivingEntity entity, @Nullable MobEffectInstance effect);
            }
        }
    }

    public static final class Chunk {
        private Chunk() {}

        /**
         * @see ServerChunkEvents#CHUNK_LOAD
         */
        @FabricAPI public static final Event<ServerChunkEvents.Load> LOAD = ServerChunkEvents.CHUNK_LOAD;

        /**
         * @see ServerChunkEvents#CHUNK_GENERATE
         */
        @FabricAPI public static final Event<ServerChunkEvents.Generate> GENERATE = ServerChunkEvents.CHUNK_GENERATE;

        /**
         * @see ServerChunkEvents#CHUNK_UNLOAD
         */
        @FabricAPI public static final Event<ServerChunkEvents.Unload> UNLOAD = ServerChunkEvents.CHUNK_UNLOAD;

        /**
         * @see ServerChunkEvents#CHUNK_LEVEL_TYPE_CHANGE
         */
        @FabricAPI public static final Event<ServerChunkEvents.LevelTypeChange> LEVEL_TYPE_CHANGE = ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE;
    }

    public static final class Entity {
        private Entity() {}

        /**
         * @see ServerEntityEvents#ENTITY_LOAD
         */
        @FabricAPI public static final Event<ServerEntityEvents.Load> LOAD = ServerEntityEvents.ENTITY_LOAD;

        /**
         * @see ServerEntityEvents#ENTITY_UNLOAD
         */
        @FabricAPI public static final Event<ServerEntityEvents.Unload> UNLOAD = ServerEntityEvents.ENTITY_UNLOAD;

        /**
         * @see ServerEntityEvents#EQUIPMENT_CHANGE
         */
        @FabricAPI public static final Event<ServerEntityEvents.EquipmentChange> EQUIPMENT_CHANGE = ServerEntityEvents.EQUIPMENT_CHANGE;

        /**
         * @see ServerEntityCombatEvents#AFTER_KILLED_OTHER_ENTITY
         */
        @FabricAPI public static final Event<ServerEntityCombatEvents.AfterKilledOtherEntity> AFTER_KILLED_OTHER_ENTITY = ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY;

        public static final class Elytra {
            private Elytra() {}

            /**
             * @see EntityElytraEvents#ALLOW
             */
            @FabricAPI public static final Event<EntityElytraEvents.Allow> ALLOW = EntityElytraEvents.ALLOW;

            /**
             * @see EntityElytraEvents#CUSTOM
             */
            @FabricAPI public static final Event<EntityElytraEvents.Custom> CUSTOM = EntityElytraEvents.CUSTOM;
        }

        public static final class Sleep {
            private Sleep() {}

            /**
             * @see EntitySleepEvents#ALLOW_SLEEPING
             */
            @FabricAPI public static final Event<EntitySleepEvents.AllowSleeping> ALLOW_SLEEPING = EntitySleepEvents.ALLOW_SLEEPING;

            /**
             * @see EntitySleepEvents#START_SLEEPING
             */
            @FabricAPI public static final Event<EntitySleepEvents.StartSleeping> START_SLEEPING = EntitySleepEvents.START_SLEEPING;

            /**
             * @see EntitySleepEvents#STOP_SLEEPING
             */
            @FabricAPI public static final Event<EntitySleepEvents.StopSleeping> STOP_SLEEPING = EntitySleepEvents.STOP_SLEEPING;

            /**
             * @see EntitySleepEvents#ALLOW_BED
             */
            @FabricAPI public static final Event<EntitySleepEvents.AllowBed> ALLOW_BED = EntitySleepEvents.ALLOW_BED;

//            /**
//             * @see EntitySleepEvents#ALLOW_SLEEP_TIME
//             */
//            @FabricAPI public static final Event<EntitySleepEvents.AllowSleepTime> ALLOW_SLEEP_TIME = EntitySleepEvents.ALLOW_SLEEP_TIME;

            /**
             * @see EntitySleepEvents#ALLOW_NEARBY_MONSTERS
             */
            @FabricAPI public static final Event<EntitySleepEvents.AllowNearbyMonsters> ALLOW_NEARBY_MONSTERS = EntitySleepEvents.ALLOW_NEARBY_MONSTERS;

            /**
             * @see EntitySleepEvents#ALLOW_RESETTING_TIME
             */
            @FabricAPI public static final Event<EntitySleepEvents.AllowResettingTime> ALLOW_RESETTING_TIME = EntitySleepEvents.ALLOW_RESETTING_TIME;

            /**
             * @see EntitySleepEvents#MODIFY_SLEEPING_DIRECTION
             */
            @FabricAPI public static final Event<EntitySleepEvents.ModifySleepingDirection> MODIFY_SLEEPING_DIRECTION = EntitySleepEvents.MODIFY_SLEEPING_DIRECTION;

            /**
             * @see EntitySleepEvents#ALLOW_SETTING_SPAWN
             */
            @FabricAPI public static final Event<EntitySleepEvents.AllowSettingSpawn> ALLOW_SETTING_SPAWN = EntitySleepEvents.ALLOW_SETTING_SPAWN;

            /**
             * @see EntitySleepEvents#SET_BED_OCCUPATION_STATE
             */
            @FabricAPI public static final Event<EntitySleepEvents.SetBedOccupationState> SET_BED_OCCUPATION_STATE = EntitySleepEvents.SET_BED_OCCUPATION_STATE;

            /**
             * @see EntitySleepEvents#MODIFY_WAKE_UP_POSITION
             */
            @FabricAPI public static final Event<EntitySleepEvents.ModifyWakeUpPosition> MODIFY_WAKE_UP_POSITION = EntitySleepEvents.MODIFY_WAKE_UP_POSITION;
        }

        public static final class Tracking {
            private Tracking() {}

            /**
             * @see EntityTrackingEvents#START_TRACKING
             */
            @FabricAPI public static final Event<EntityTrackingEvents.StartTracking> START = EntityTrackingEvents.START_TRACKING;

            /**
             * @see EntityTrackingEvents#STOP_TRACKING
             */
            @FabricAPI public static final Event<EntityTrackingEvents.StopTracking> STOP = EntityTrackingEvents.STOP_TRACKING;
        }
    }

    public static final class Lifecycle {
        private Lifecycle() {}

        /**
         * @see ServerLifecycleEvents#SERVER_STARTING
         */
        @FabricAPI public static final Event<ServerLifecycleEvents.ServerStarting> STARTING = ServerLifecycleEvents.SERVER_STARTING;

        /**
         * @see ServerLifecycleEvents#SERVER_STARTED
         */
        @FabricAPI public static final Event<ServerLifecycleEvents.ServerStarted> STARTED = ServerLifecycleEvents.SERVER_STARTED;

        /**
         * @see ServerLifecycleEvents#SERVER_STOPPING
         */
        @FabricAPI public static final Event<ServerLifecycleEvents.ServerStopping> STOPPING = ServerLifecycleEvents.SERVER_STOPPING;

        /**
         * @see ServerLifecycleEvents#SERVER_STOPPED
         */
        @FabricAPI public static final Event<ServerLifecycleEvents.ServerStopped> STOPPED = ServerLifecycleEvents.SERVER_STOPPED;

        /**
         * @see CommonLifecycleEvents#TAGS_LOADED
         */
        @FabricAPI public static final Event<CommonLifecycleEvents.TagsLoaded> TAGS_LOADED = CommonLifecycleEvents.TAGS_LOADED;

        public static final class DataPack {
            private DataPack() {}

            /**
             * @see ServerLifecycleEvents#SYNC_DATA_PACK_CONTENTS
             */
            @FabricAPI public static final Event<ServerLifecycleEvents.SyncDataPackContents> SYNC_CONTENTS = ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS;

            public static final class Reload {
                private Reload() {}

                /**
                 * @see ServerLifecycleEvents#START_DATA_PACK_RELOAD
                 */
                @FabricAPI public static final Event<ServerLifecycleEvents.StartDataPackReload> START = ServerLifecycleEvents.START_DATA_PACK_RELOAD;

                /**
                 * @see ServerLifecycleEvents#END_DATA_PACK_RELOAD
                 */
                @FabricAPI public static final Event<ServerLifecycleEvents.EndDataPackReload> END = ServerLifecycleEvents.END_DATA_PACK_RELOAD;
            }
        }

        public static final class Save {
            private Save() {}

            /**
             * @see ServerLifecycleEvents#BEFORE_SAVE
             */
            @FabricAPI public static final Event<ServerLifecycleEvents.BeforeSave> BEFORE = ServerLifecycleEvents.BEFORE_SAVE;

            /**
             * @see ServerLifecycleEvents#AFTER_SAVE
             */
            @FabricAPI public static final Event<ServerLifecycleEvents.AfterSave> AFTER = ServerLifecycleEvents.AFTER_SAVE;
        }
    }

    public static final class Tick {
        private Tick() {}

        public static final class Server {
            private Server() {}

            /**
             * @see ServerTickEvents#START_SERVER_TICK
             */
            @FabricAPI public static final Event<ServerTickEvents.StartTick> START = ServerTickEvents.START_SERVER_TICK;

            /**
             * @see ServerTickEvents#END_SERVER_TICK
             */
            @FabricAPI public static final Event<ServerTickEvents.EndTick> END = ServerTickEvents.END_SERVER_TICK;
        }

        public static final class World {
            private World() {}

            /**
             * @see ServerTickEvents#START_WORLD_TICK
             */
            @FabricAPI public static final Event<ServerTickEvents.StartWorldTick> START = ServerTickEvents.START_WORLD_TICK;

            /**
             * @see ServerTickEvents#END_WORLD_TICK
             */
            @FabricAPI public static final Event<ServerTickEvents.EndWorldTick> END = ServerTickEvents.END_WORLD_TICK;
        }
    }

    public static final class Message {
        private Message() {}

        public static final class Chat {
            private Chat() {}

            /**
             * @see ServerMessageEvents#ALLOW_CHAT_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.AllowChatMessage> ALLOW = ServerMessageEvents.ALLOW_CHAT_MESSAGE;

            /**
             * @see ServerMessageEvents#CHAT_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.ChatMessage> ON = ServerMessageEvents.CHAT_MESSAGE;
        }

        public static final class Game {
            private Game() {}

            /**
             * @see ServerMessageEvents#ALLOW_GAME_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.AllowGameMessage> ALLOW = ServerMessageEvents.ALLOW_GAME_MESSAGE;

            /**
             * @see ServerMessageEvents#GAME_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.GameMessage> ON = ServerMessageEvents.GAME_MESSAGE;
        }

        public static final class Command {
            private Command() {}

            /**
             * @see ServerMessageEvents#ALLOW_COMMAND_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.AllowCommandMessage> ALLOW = ServerMessageEvents.ALLOW_COMMAND_MESSAGE;

            /**
             * @see ServerMessageEvents#COMMAND_MESSAGE
             */
            @FabricAPI public static final Event<ServerMessageEvents.CommandMessage> ON = ServerMessageEvents.COMMAND_MESSAGE;
        }
    }

    public static final class World {
        private World() {}

        /**
         * @see ServerWorldEvents#LOAD
         */
        @FabricAPI public static final Event<ServerWorldEvents.Load> LOAD = ServerWorldEvents.LOAD;

        /**
         * @see ServerWorldEvents#UNLOAD
         */
        @FabricAPI public static final Event<ServerWorldEvents.Unload> UNLOAD = ServerWorldEvents.UNLOAD;

        public static final class Change {
            private Change() {}

            /**
             * @see ServerEntityWorldChangeEvents#AFTER_ENTITY_CHANGE_WORLD
             */
            @FabricAPI public static final Event<ServerEntityWorldChangeEvents.AfterEntityChange> ENTITY = ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD;

            /**
             * @see ServerEntityWorldChangeEvents#AFTER_PLAYER_CHANGE_WORLD
             */
            @FabricAPI public static final Event<ServerEntityWorldChangeEvents.AfterPlayerChange> PLAYER = ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD;
        }
    }

    public static final class BlockEntity {
        private BlockEntity() {}

        /**
         * @see ServerBlockEntityEvents#BLOCK_ENTITY_LOAD
         */
        @FabricAPI public static final Event<ServerBlockEntityEvents.Load> LOAD = ServerBlockEntityEvents.BLOCK_ENTITY_LOAD;

        /**
         * @see ServerBlockEntityEvents#BLOCK_ENTITY_UNLOAD
         */
        @FabricAPI public static final Event<ServerBlockEntityEvents.Unload> UNLOAD = ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD;
    }

    public static final class LootTable {
        private LootTable() {}

        /**
         * @see LootTableEvents#REPLACE
         */
        @FabricAPI public static final Event<LootTableEvents.Replace> REPLACE = LootTableEvents.REPLACE;

        /**
         * @see LootTableEvents#MODIFY
         */
        @FabricAPI public static final Event<LootTableEvents.Modify> MODIFY = LootTableEvents.MODIFY;

        /**
         * @see LootTableEvents#ALL_LOADED
         */
        @FabricAPI public static final Event<LootTableEvents.Loaded> ALL_LOADED = LootTableEvents.ALL_LOADED;
    }

    static {
        ServerEvents.Player.Join.MODIFY_MESSAGE.register((player, message) -> ServerEvents.Player.MODIFY_JOIN_MESSAGE.invoker().modifyJoinMessage(player, message));
        ServerEvents.Player.Leave.MODIFY_MESSAGE.register((player, message) -> ServerEvents.Player.MODIFY_LEAVE_MESSAGE.invoker().modifyLeaveMessage(player, message));
    }
}
