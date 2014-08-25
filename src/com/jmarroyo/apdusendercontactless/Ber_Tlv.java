/*
Copyright 2014  Jose Maria ARROYO jm.arroyo.castejon@gmail.com

APDUSenderContactLess is free software: you can redistribute it and/or modify
it  under  the  terms  of the GNU General Public License  as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) 
any later version.

APDUSenderContactLess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.jmarroyo.apdusendercontactless;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Ber_Tlv
{

    private byte[] TagHex;
    private byte[] rawEncodedLengthBytes;
    private byte[] valueBytes;


    private static boolean isBitSet(byte data, int offset)
    {
        if ((data >> (offset - 1) & 0x01) == 1) 
        {
            return true;
        }
        return false;
    }

    private static int iatoi(byte[] data, int length)
    {
        int atoi = 0;
        for (int ii = 0; ii < length; ii++)
        {
            atoi += ((data[ii] & 0xFF) << 8 * (data.length - ii - 1));
        }
        return atoi;
    }

    private static byte[] get_TLV_Tag(ByteArrayInputStream stream)
    {
        ByteArrayOutputStream tagBAOS = new ByteArrayOutputStream();
        byte tagFirstOctet = (byte) stream.read();
        byte MASK = (byte)0x1F;

        tagBAOS.write(tagFirstOctet);
        if ((tagFirstOctet & MASK) == MASK)
        {
            do
            {
                int nextOctet = stream.read();
                if(nextOctet < 0)
                {
                    break;
                }
                byte tlvIdNextOctet = (byte) nextOctet;
                tagBAOS.write(tlvIdNextOctet);
                if (!isBitSet(tlvIdNextOctet, 8))
                {
                    break;
                }
            }while (true);
        }
        return tagBAOS.toByteArray();
    }
         
    private static int get_TLV_Length(ByteArrayInputStream stream)
    {
        int length;
        int length_aux = stream.read();

        if (length_aux <= 128)
        {
            length = length_aux;
        }
        else 
        {
            int numberOfLengthOctets = length_aux & 127;
            length_aux = 0;
            for (int i = 0; i < numberOfLengthOctets; i++)
            {
                int nextLengthOctet = stream.read();
                length_aux <<= 8;
                length_aux |= nextLengthOctet;
            }
            length = length_aux;
        }
        return length;
    }

    private Ber_Tlv(byte[] TagHex,int length, byte[] rawEncodedLengthBytes, byte[] valueBytes)
    {
        if(valueBytes!=null)
        {
            this.valueBytes = valueBytes;
        }
        this.rawEncodedLengthBytes = rawEncodedLengthBytes;
        this.TagHex = TagHex;
    }

    public static Ber_Tlv getNextTLV(ByteArrayInputStream stream, boolean bDOL)
    {
        stream.mark(0);
        int peekInt = stream.read();
        byte peekByte = (byte) peekInt;
        while( (peekInt!=-1) && ( (peekByte==(byte)0xFF) || (peekByte == (byte)0x00) ) )
        {
            stream.mark(0);
            peekInt = stream.read();
            peekByte = (byte)peekInt;
        }
        stream.reset();

        // TAG
        byte[] tagIdBytes = get_TLV_Tag(stream);

        stream.mark(0);
        int posBefore = stream.available();

        // LENGTH
        int length = get_TLV_Length(stream);
        int posAfter = stream.available();
        stream.reset();
        byte[] lengthBytes = new byte[posBefore - posAfter];
        stream.read(lengthBytes, 0, lengthBytes.length);

        int rawLength = iatoi(lengthBytes,lengthBytes.length);
        byte[] valueBytes = null;

        if(bDOL==true)
        {
            Ber_Tlv tlv = new Ber_Tlv(tagIdBytes, length, lengthBytes, null);
            return tlv;
        }

        // VALUE
        if (rawLength == 128)
        {
            stream.mark(0);
            int prevOctet = 1;
            int curOctet = 0;
            int len = 0;
            while(true)
            {
                len++;
                curOctet = stream.read();
                if( (prevOctet == 0) && (curOctet == 0) )
                {
                    break;
                }
                prevOctet = curOctet;
            }
            len -= 2;
            valueBytes = new byte[len];
            stream.reset();
            stream.read(valueBytes, 0, len);
            length = len;
        }
        else
        {
            valueBytes = new byte[length];
            stream.read(valueBytes, 0, length);
        }
        stream.mark(0);
        peekInt = stream.read();
        peekByte = (byte) peekInt;
        while (peekInt != -1 && (peekByte == (byte) 0xFF || peekByte == (byte) 0x00))
        {
            stream.mark(0);
            peekInt = stream.read();
            peekByte = (byte) peekInt;
        }
        stream.reset();

        Ber_Tlv tlv = new Ber_Tlv(tagIdBytes, length, lengthBytes, valueBytes);
        return tlv;
    }

    public byte[] getTagBytes()
    {
        return TagHex;
    }

    public byte[] getRawEncodedLengthBytes()
    {
        return rawEncodedLengthBytes;
    }

    public byte[] getValueBytes()
    {
        return valueBytes;
    }

}
