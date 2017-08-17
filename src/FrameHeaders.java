/**
 * Created by RicardoFS on 16/08/2017.
 */
public enum FrameHeaders{
    SKIP((byte)0),
    ONLY_APPLY_MOVEMENTS((byte)1),
    APPLY_MOVEMENT_TRANSLATE_MOVEMENT((byte)2);

    private final byte header;
    FrameHeaders(byte header) { this.header = header; }
    public byte getHeaderByte() { return header; }
    public static FrameHeaders getHeader(byte b) {
        if(b == SKIP.getHeaderByte()) return SKIP;
        else if(b == ONLY_APPLY_MOVEMENTS.getHeaderByte()) return ONLY_APPLY_MOVEMENTS;
        else if(b == APPLY_MOVEMENT_TRANSLATE_MOVEMENT.getHeaderByte()) return APPLY_MOVEMENT_TRANSLATE_MOVEMENT;
        return SKIP;
    }
}
