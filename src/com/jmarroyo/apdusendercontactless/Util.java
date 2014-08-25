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

public class Util
{

    public static String szByteHex2String(byte datain)
    {
        String[] CHARS0F = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        int idata = datain & 0xFF;
        int nibble_1 = (idata >> 0x04) & 0x0F;
        int nibble_2 = idata & 0x0F;

        return CHARS0F[nibble_1] + CHARS0F[nibble_2];
    }

}
