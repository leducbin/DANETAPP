/*
 * Copyright (C) 2011-2012 George Yunaev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

/*
   You can find the opensource file here: http://www.ulduzsoft.com/2012/07/parsing-id3v2-tags-in-the-mp3-files/
   The list of modifications done compared to the opensource file are:
    - rename the class MetaInfoParser_MP3 by LVID3MetadataParser
    - parse ByteBuffer instead of File
    - Fix bad parsing of Id3v4 (framesize)
    - parse new frames (APIC, TXXX, GEOB ...)
    - new APIs to get new frames (APIC, TXXX ...)
    Description for all tags can be found here : http://id3.org/id3v2.3.0
*/

package com.movideo.whitelabel.widgetutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.movideo.whitelabel.widgetutils.WidgetsUtils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Class for id3 buffer parsing.<br>
 * The purpose of this class is to provide a method to parse the content of ID3 buffer<br>
 * like (picture, text...).<br>
 */
public class LVID3MetadataParser {

    private final String TAG  =  "LVID3MetadataParser"; // Tag used for Android logging

    // Stores the data
    private String m_artist = null;
    private String m_title = null;
    private String m_textInfo = null;
    private String m_PrivOwnerIDentifier = null;
    private String m_GeobSubtitle = null;
    private Bitmap m_embeddedPicture = null;

    /**
     * Returns the artist in ID3 buffer (value of frame: TPE1 ,TPE2, TPE3 or TPE).
     * You should call {@link #parse()} before this function.
     * @return A String equals to the artist found in ID3Buffer.
     */
    public String getArtist()
    {
        return m_artist;
    }

    /**
     * Returns the title in ID3 buffer (value of frame: TIT or TIT2).
     * You should call {@link #parse()} before this function.
     * @return A String equals to the artist found in ID3Buffer.
     */
    public String getTitle()
    {
        return m_title;
    }

    /**
     * Returns the text info in ID3 buffer (value of frame: TXXX).
     * You should call {@link #parse()} before this function.
     * @return A String equals to the text info found in ID3Buffer.
     */
    public String getTextInfo()
    {
        return m_textInfo;
    }

    /**
     * Returns the subtitles info ID3 buffer (value of frame: GEOB).
     * You should call {@link #parse()} before this function.
     * @return A String equals to the text info found in ID3Buffer.
     */
    public String getGeobSubtitle()
    {
        return m_GeobSubtitle;
    }

    /**
     * Returns the owner identifier in ID3 buffer (value of frame: PRIV).
     * You should call {@link #parse()} before this function.
     * @return A String equals to the owner identifier found in ID3Buffer.
     */
    public String getPrivOwnerIdentifier()
    {
        return m_PrivOwnerIDentifier;
    }

    /**
     * Returns the picture found in ID3 buffer (value of frame: APIC or PIC).
     * You should call {@link #parse()} before this function.
     * @return A Bitmap containing the picture found in ID3Buffer.
     */
    public Bitmap getEmbeddedPicture()
    {
        return m_embeddedPicture;
    }

    /**
     * Parse the ID3 buffer given in constructor.<br>
     * This function gets ID3 frames and store them. Then, ID3 frames can be accessible via get methods.
     * @param ByteBuffer to parse.
     * @return true if the buffer contains the ID3 tag, and at least one recognized frame (TXXX, APIC etc...)
     * @throws BufferUnderflowException if the buffer is not enough big.
     */
    public boolean parse(ByteBuffer id3Buffer){

        //reset variables
        m_artist = null;
        m_title = null;
        m_textInfo = null;
        m_embeddedPicture = null;
        m_GeobSubtitle = null;
        boolean errorParsing = false;

        try{
            byte[] headerbuf = new byte[10];
            id3Buffer.get( headerbuf );

            // Parse it quickly
            if ( headerbuf[0] != 'I' || headerbuf[1] != 'D' || headerbuf[2] != '3' )
            {
                Log.e(TAG, "LVID3MetadataParser bad format (ID3 not found)");
                return false;
            }

            // True if the tag is pre-V3 tag (shorter headers)
            final int TagVersion = headerbuf[3];

            // Check the version
            if ( TagVersion < 0 || TagVersion > 4 )
            {
                Log.e(TAG, "LVID3MetadataParser unsupported version "+ TagVersion);
                return false;
            }

            // Get the ID3 tag size and flags; see 3.1
            int tagsize = (headerbuf[9] & 0xFF) | ((headerbuf[8] & 0xFF) << 7 ) | ((headerbuf[7] & 0xFF) << 14 ) | ((headerbuf[6] & 0xFF) << 21 ) + 10;
            boolean uses_synch = (headerbuf[5] & 0x80) != 0 ? true : false;
            boolean has_extended_hdr = (headerbuf[5] & 0x40) != 0 ? true : false;

            // Read the extended header length and skip it
            if ( has_extended_hdr )
            {
                int headersize = id3Buffer.get() << 21 | id3Buffer.get() << 14 | id3Buffer.get() << 7 | id3Buffer.get();
                byte[] extendedHeaderBuf = new byte[headersize - 4];
                id3Buffer.get(extendedHeaderBuf);
            }

            // Read the whole tag
            if (tagsize > id3Buffer.remaining()){
                tagsize = id3Buffer.remaining();
            }
            byte[] buffer = new byte[ tagsize ];
            id3Buffer.get(buffer);

            // Prepare to parse the tag
            int length = buffer.length;

            // Recreate the tag if desynchronization is used inside; we need to replace 0xFF 0x00 with 0xFF
            if ( uses_synch )
            {
                int newpos = 0;
                byte[] newbuffer = new byte[ tagsize ];

                for ( int i = 0; i < buffer.length; i++ )
                {
                    if ( i < buffer.length - 1 && (buffer[i] & 0xFF) == 0xFF && buffer[i+1] == 0 )
                    {
                        newbuffer[newpos++] = (byte) 0xFF;
                        i++;
                        continue;
                    }

                    newbuffer[newpos++] = buffer[i];
                }

                length = newpos;
                buffer = newbuffer;
            }

            // Set some params
            int pos = 0;
            final int ID3FrameSize = TagVersion < 3 ? 6 : 10;

            // Parse the tags
            while ( true )
            {
                int rembytes = length - pos;

                // Do we have the frame header?
                if ( rembytes < ID3FrameSize )
                    break;

                // Is there a frame?
                if ( buffer[pos] < 'A' || buffer[pos] > 'Z' )
                    break;

                // Frame name is 3 chars in pre-ID3v3 and 4 chars after
                String framename;
                int framesize;

                if ( TagVersion < 3 )
                {
                    framename = new String( buffer, pos, 3 );
                    framesize = ((buffer[pos+5] & 0xFF) << 8 ) | ((buffer[pos+4] & 0xFF) << 16 ) | ((buffer[pos+3] & 0xFF) << 24 );
                }
                else
                {
                    framename = new String( buffer, pos, 4 );
                    if (TagVersion == 3){
                        framesize = (buffer[pos+7] & 0xFF) | ((buffer[pos+6] & 0xFF) << 8 ) | ((buffer[pos+5] & 0xFF) << 16 ) | ((buffer[pos+4] & 0xFF) << 24 );
                    }else{
                        //NXP modif: compute framesize for id3v4
                        framesize = (buffer[pos+7] & 0xFF) | ((buffer[pos+6] & 0xFF) << 7 ) | ((buffer[pos+5] & 0xFF) << 20 ) | ((buffer[pos+4] & 0xFF) << 21 );
                    }
                }

                if ( pos + framesize > length ){
                    //NXP modif: set the maximum size for framesize
                    framesize = length - ID3FrameSize;
                }

                if ( framename.equals( "TPE1" ) || framename.equals( "TPE2" ) || framename.equals( "TPE3" ) || framename.equals( "TPE" ) )
                {
                    if ( m_artist == null )
                        m_artist = parseTextField( buffer, pos + ID3FrameSize, framesize );
                }
                else if ( framename.equals( "TIT2" ) || framename.equals( "TIT" ) )
                {
                    if ( m_title == null )
                        m_title = parseTextField( buffer, pos + ID3FrameSize, framesize );
                }
                else if ( framename.equals( "TXXX" ))
                {
                    if ( m_textInfo == null )
                        m_textInfo = parseTextField( buffer, pos + ID3FrameSize, framesize ).trim();

                    Log.d(TAG, "LVID3MetadataParser parse m_textInfo = " + m_textInfo.trim());
                }
                else if ( framename.equals( "APIC" ) || framename.equals( "PIC" ))
                {
                    if ( m_embeddedPicture == null ){
                        m_embeddedPicture = parsePictureField( buffer, pos + ID3FrameSize, framesize );
                    }
                    if (m_embeddedPicture == null){
                        //the parsing has failed. We will return an error at the end of the parsing.
                        errorParsing = true;
                    }
                }
                else if ( framename.equals( "GEOB" ))
                {
                    if ( m_GeobSubtitle == null ){
                        m_GeobSubtitle = parseGEOBField( buffer, pos + ID3FrameSize, framesize );
                    }
                    if (m_GeobSubtitle == null){
                        //the parsing has failed. We will return an error at the end of the parsing.
                        errorParsing = true;
                    }
                    Log.d(TAG, "LVID3MetadataParser parse m_GeobSubtitle = " + m_GeobSubtitle);
                }
                else if ( framename.equals( "PRIV" ) )
                {
                    if ( m_PrivOwnerIDentifier == null )
                        m_PrivOwnerIDentifier = parseTextField( buffer, pos + ID3FrameSize, framesize);
                }
                else
                {
                    Log.e(TAG, "LVID3MetadataParser parsing error (tag +"+framename+" not recognized!)");
                }

                pos += framesize + ID3FrameSize;
                continue;
            }

            return (m_title != null || m_artist != null || m_textInfo != null || m_embeddedPicture != null || m_PrivOwnerIDentifier != null || m_GeobSubtitle != null ) && (false == errorParsing) ;
        } catch (Exception e) {
            Log.e(TAG, "LVID3MetadataParser parsing exception!");
            e.printStackTrace();
            return false;
        }
    }

    private String parseTextField( final byte[] buffer, int pos, int size )
    {
        if ( size < 2 )
            return null;

        Charset charset;
        int charcode = buffer[pos];

        if ( charcode == 0 )
            charset = Charset.forName("ISO-8859-1");
        else if ( charcode == 3 )
            charset = Charset.forName("UTF-8");
        else
            charset = Charset.forName("UTF-16");

        return charset.decode( ByteBuffer.wrap(buffer, pos + 1, size - 1) ).toString();
    }

    private String parseGEOBField(final byte[] buffer, int pos, int size) {
         String subtitles = null;
         try{
             int startPoint = pos;
             int refPoint = startPoint;
             //1) we get the encoding type
             int encodingType = (buffer[refPoint++] & 0xFF); //0=ISO8859, 1=Unicode,2=UnicodeBE,3=UTF8
             //2) we get the mime type
             int indexPoint = refPoint;
             while ((refPoint < buffer.length) &&  (buffer[refPoint++] != 0)) {
             }
             int mimeLength = refPoint - indexPoint;
             String mimeType = null;
             if (mimeLength > 1) {
                 mimeType = new String(buffer, indexPoint, mimeLength - 1, "ISO-8859-1");
             }
             //3) we get the Filename
             indexPoint = refPoint;
             while ((refPoint < buffer.length) && (buffer[refPoint++] != 0)) {
             }
             int filenameLength = refPoint - indexPoint;
             String filename = null;
             if (filenameLength > 1) {
                 if (0 == encodingType){
                     filename = new String(buffer, indexPoint, filenameLength - 1, "ISO8859");
                 }else if (1 == filenameLength){
                     filename = new String(buffer, indexPoint, filenameLength - 1, "Unicode");
                 }else if (2 == filenameLength){
                     filename = new String(buffer, indexPoint, filenameLength - 1, "UnicodeBE");
                 }else{
                     filename = new String(buffer, indexPoint, filenameLength - 1, "UTF8");
                 }
             }
             //4) we get the GEOB description
             indexPoint = refPoint;
             while ((refPoint < buffer.length) && (buffer[refPoint++] != 0)) {
             }
             int descriptionLength = refPoint - indexPoint;
             String description = null;
             if (descriptionLength > 1) {
                 description = new String(buffer, indexPoint, descriptionLength - 1, "ISO-8859-1");
             }
             //5) get data
             if (description.contains("subtitles")){
                 subtitles = new String(buffer ,refPoint, buffer.length - refPoint, "ISO-8859-1");
             }

             Log.d(TAG, "LVID3MetadataParser parseGEOBField: mimeType=" + mimeType + ", encodingType=" + encodingType + ", filename="+ filename + ", description=" + description+ ", subtitles="+ subtitles);

         }catch (Exception e){
             e.printStackTrace();
             Log.e(TAG, "LVID3MetadataParser parseGEOBField: exception when parsing picture");
         }
         return subtitles;
    }

    private Bitmap parsePictureField( final byte[] buffer, int pos, int size ) {
        Bitmap decodedPicture = null;
        try{
            int startPoint = pos;
            int refPoint = startPoint;
            //1) we get the encoding type
            int encodingType = (buffer[refPoint++] & 0xFF); //0=ISO8859, 1=Unicode,2=UnicodeBE,3=UTF8
            //2) we get the mime type
            int indexPoint = refPoint;
            while ((refPoint < buffer.length) &&  (buffer[refPoint++] != 0)) {
            }
            int mimeLength = refPoint - indexPoint;
            String mimeType = null;
            if (mimeLength > 1) {
                mimeType = new String(buffer, indexPoint, mimeLength - 1, "ISO-8859-1");
            }
            //3) we get the picture type
            int picType = (buffer[refPoint++] & 0xFF);
            //4) we get the picture description
            indexPoint = refPoint;
            while ((refPoint < buffer.length) && (buffer[refPoint++] != 0)) {
            }
            int descriptionLength = refPoint - indexPoint;
            String description = null;
            if (descriptionLength > 1) {
                description = new String(buffer, indexPoint, descriptionLength - 1, "ISO-8859-1");
            }
            decodedPicture = BitmapFactory.decodeByteArray(buffer, refPoint, size - (refPoint - startPoint));

            Log.d(TAG, "LVID3MetadataParser parsePictureField: mimeType=" + mimeType + ", encodingType=" + encodingType + ", picType="+ picType + ", description=" + description + ", bitmap=" + decodedPicture);

            if (null == decodedPicture){
                Log.e(TAG, "LVID3MetadataParser parsePictureField: impossible to decode the picture!");
            }
        }catch (Exception e){
            e.printStackTrace();
            decodedPicture = null;
            Log.e(TAG, "LVID3MetadataParser parsePictureField: exception when parsing picture");
        }
        return decodedPicture;
    }
}
