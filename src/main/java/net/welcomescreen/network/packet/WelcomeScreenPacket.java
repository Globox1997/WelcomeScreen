package net.welcomescreen.network.packet;

import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

//DebugBrainCustomPayload

public record WelcomeScreenPacket(ScreenData screenData, TextData textData, ImageData imageData, ButtonData buttonData) implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, WelcomeScreenPacket> PACKET_CODEC = CustomPayload.codecOf(WelcomeScreenPacket::write, WelcomeScreenPacket::new);

    public static final CustomPayload.Id<WelcomeScreenPacket> PACKET_ID = new CustomPayload.Id<>(new Identifier("welcomescreen", "welcomescreen_packet"));

    private WelcomeScreenPacket(PacketByteBuf buf) {
        this(new ScreenData(buf), new TextData(buf), new ImageData(buf), new ButtonData(buf));
    }

    private void write(PacketByteBuf buf) {
        this.screenData.write(buf);
        this.textData.write(buf);
        this.imageData.write(buf);
        this.buttonData.write(buf);
    }

    public record ScreenData(String titleText, int titleX, int titleY, boolean titleCenter, boolean titleObjectCenter, String closeText, int closeX, int closeY, boolean closeCenter,
            boolean closeObjectCenter, Identifier backgroundIdentifier, int backgroundX, int backgroundY, boolean backgroundCenter, boolean backgroundObjectCenter) {

        private ScreenData(PacketByteBuf buf) {
            this(buf.readString(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readString(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean(),
                    buf.readIdentifier(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean());
        }

        public void write(PacketByteBuf buf) {
            buf.writeString(this.titleText);
            buf.writeInt(this.titleX);
            buf.writeInt(this.titleY);
            buf.writeBoolean(this.titleCenter);
            buf.writeBoolean(this.titleObjectCenter);
            buf.writeString(this.closeText);
            buf.writeInt(this.closeX);
            buf.writeInt(this.closeY);
            buf.writeBoolean(this.closeCenter);
            buf.writeBoolean(this.closeObjectCenter);
            buf.writeIdentifier(this.backgroundIdentifier);
            buf.writeInt(this.backgroundX);
            buf.writeInt(this.backgroundY);
            buf.writeBoolean(this.backgroundCenter);
            buf.writeBoolean(this.backgroundObjectCenter);
        }
    }

    public record TextData(int textListSize, List<Integer> textPosXList, List<Integer> textPosYList, List<Boolean> textCenterList, List<Boolean> textObjectCenterList,
            List<Integer> textStringSizeList, List<String> textStringList) {

        private TextData(PacketByteBuf buf) {
            this(buf.readInt(), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readBoolean), buf.readList(PacketByteBuf::readBoolean),
                    buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readString));
        }

        public void write(PacketByteBuf buf) {
            buf.writeInt(this.textListSize);
            buf.writeCollection(this.textPosXList, PacketByteBuf::writeInt);
            buf.writeCollection(this.textPosYList, PacketByteBuf::writeInt);
            buf.writeCollection(this.textCenterList, PacketByteBuf::writeBoolean);
            buf.writeCollection(this.textObjectCenterList, PacketByteBuf::writeBoolean);
            buf.writeCollection(this.textStringSizeList, PacketByteBuf::writeInt);
            buf.writeCollection(this.textStringList, PacketByteBuf::writeString);
        }
    }

    public record ImageData(int imageListSize, List<Integer> imagePosXList, List<Integer> imagePosYList, List<Integer> imageSizeXList, List<Integer> imageSizeYList,
            List<Identifier> imageIdentifierList, List<Boolean> imageCenterList, List<Boolean> imageObjectCenterList) {

        private ImageData(PacketByteBuf buf) {
            this(buf.readInt(), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt),
                    buf.readList(PacketByteBuf::readIdentifier), buf.readList(PacketByteBuf::readBoolean), buf.readList(PacketByteBuf::readBoolean));
        }

        public void write(PacketByteBuf buf) {
            buf.writeInt(this.imageListSize);
            buf.writeCollection(this.imagePosXList, PacketByteBuf::writeInt);
            buf.writeCollection(this.imagePosYList, PacketByteBuf::writeInt);
            buf.writeCollection(this.imageSizeXList, PacketByteBuf::writeInt);
            buf.writeCollection(this.imageSizeYList, PacketByteBuf::writeInt);
            buf.writeCollection(this.imageIdentifierList, PacketByteBuf::writeIdentifier);
            buf.writeCollection(this.imageCenterList, PacketByteBuf::writeBoolean);
            buf.writeCollection(this.imageObjectCenterList, PacketByteBuf::writeBoolean);
        }
    }

    public record ButtonData(int buttonListSize, List<Integer> buttonPosXList, List<Integer> buttonPosYList, List<Integer> buttonSizeXList, List<Integer> buttonSizeYList,
            List<String> buttonStringList, List<String> buttonLinkList, List<Boolean> buttonCenterList, List<Boolean> buttonObjectCenterList) {

        private ButtonData(PacketByteBuf buf) {
            this(buf.readInt(), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt),
                    buf.readList(PacketByteBuf::readString), buf.readList(PacketByteBuf::readString), buf.readList(PacketByteBuf::readBoolean), buf.readList(PacketByteBuf::readBoolean));
        }

        public void write(PacketByteBuf buf) {
            buf.writeInt(this.buttonListSize);
            buf.writeCollection(this.buttonPosXList, PacketByteBuf::writeInt);
            buf.writeCollection(this.buttonPosYList, PacketByteBuf::writeInt);
            buf.writeCollection(this.buttonSizeXList, PacketByteBuf::writeInt);
            buf.writeCollection(this.buttonSizeYList, PacketByteBuf::writeInt);
            buf.writeCollection(this.buttonStringList, PacketByteBuf::writeString);
            buf.writeCollection(this.buttonLinkList, PacketByteBuf::writeString);
            buf.writeCollection(this.buttonCenterList, PacketByteBuf::writeBoolean);
            buf.writeCollection(this.buttonObjectCenterList, PacketByteBuf::writeBoolean);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
