package com.github.trganda.codec.auths;

import com.github.trganda.codec.MySQLServerPacketVisitor;
import com.github.trganda.codec.constants.CapabilityFlags;
import com.github.trganda.codec.constants.Constants;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.constants.ServerStatusFlag;
import com.github.trganda.codec.packets.MySQLServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AsciiString;

import java.util.*;

public class Handshake extends DefaultByteBufHolder implements MySQLServerPacket {

    public static final int DEFAULT_PROTOCOL_VERSION = 10;

    private final int protocolVersion;
    private final AsciiString serverVersion;
    private final int connectionId;
    private final Set<CapabilityFlags> capabilities;
    private final MySQLCharacterSet characterSet;
    private final Set<ServerStatusFlag> serverStatus;
    private final String authPluginName;

    private Handshake(Builder builder) {
        super(builder.authPluginData);
        if (builder.authPluginData.readableBytes() < Constants.AUTH_PLUGIN_DATA_PART1_LEN) {
            throw new IllegalArgumentException(
                "authPluginData can not contain less than "
                    + Constants.AUTH_PLUGIN_DATA_PART1_LEN
                    + " bytes.");
        }

        //		System.out.println(">>>" + builder.authPluginData.readableBytes() + " " +
        // Arrays.toString(this.getAuthPluginData().array()));

        protocolVersion = builder.protocolVersion;
        if (builder.serverVersion == null) {
            throw new NullPointerException("serverVersion can not be null");
        }
        serverVersion = AsciiString.of(builder.serverVersion);
        connectionId = builder.connectionId;
        capabilities = Collections.unmodifiableSet(builder.capabilities);
        characterSet = builder.characterSet;
        serverStatus = Collections.unmodifiableSet(builder.serverStatus);
        authPluginName = builder.authPluginName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public AsciiString getServerVersion() {
        return serverVersion;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public ByteBuf getAuthPluginData() {
        return content();
    }

    public Set<CapabilityFlags> getCapabilities() {
        return capabilities;
    }

    public MySQLCharacterSet getCharacterSet() {
        return characterSet;
    }

    public Set<ServerStatusFlag> getServerStatus() {
        return serverStatus;
    }

    public String getAuthPluginName() {
        return authPluginName;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Handshake handshake = (Handshake) o;
        return protocolVersion == handshake.protocolVersion
            && connectionId == handshake.connectionId
            && Objects.equals(serverVersion, handshake.serverVersion)
            && Objects.equals(capabilities, handshake.capabilities)
            && characterSet == handshake.characterSet
            && Objects.equals(serverStatus, handshake.serverStatus)
            && Objects.equals(authPluginName, handshake.authPluginName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            protocolVersion,
            serverVersion,
            connectionId,
            capabilities,
            characterSet,
            serverStatus,
            authPluginName);
    }

    public static class Builder extends AbstractAuthPluginDataBuilder<Builder> {
        private int protocolVersion = DEFAULT_PROTOCOL_VERSION;
        private CharSequence serverVersion;
        private int connectionId = -1;

        private MySQLCharacterSet characterSet = MySQLCharacterSet.DEFAULT;
        private Set<ServerStatusFlag> serverStatus = EnumSet.noneOf(ServerStatusFlag.class);
        private String authPluginName = Constants.DEFAULT_AUTH_PLUGIN_NAME;

        public Builder protocolVersion(int protocolVerison) {
            this.protocolVersion = protocolVerison;
            return this;
        }

        public Builder serverVersion(CharSequence serverVersion) {
            this.serverVersion = serverVersion;
            return this;
        }

        public Builder connectionId(int connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public Builder characterSet(MySQLCharacterSet characterSet) {
            this.characterSet = characterSet == null ? MySQLCharacterSet.DEFAULT : characterSet;
            return this;
        }

        public Builder addServerStatus(
            ServerStatusFlag serverStatus, ServerStatusFlag... serverStatuses) {
            this.serverStatus.add(serverStatus);
            Collections.addAll(this.serverStatus, serverStatuses);
            return this;
        }

        public Builder addServerStatus(Collection<ServerStatusFlag> serverStatus) {
            this.serverStatus.addAll(serverStatus);
            return this;
        }

        public Builder authPluginName(String authPluginName) {
            capabilities.add(CapabilityFlags.CLIENT_PLUGIN_AUTH);
            this.authPluginName = authPluginName;

            System.out.println("authPluginName = " + authPluginName);

            return this;
        }

        public Handshake build() {
            return new Handshake(this);
        }
    }

    @Override
    public void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
        visitor.visit(this, ctx);
    }
}
