/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/* @test
   @summary Test SoftChannel noteOn/noteOff overflow test */

import javax.sound.midi.MidiChannel;
import javax.sound.midi.VoiceStatus;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.sun.media.sound.AudioSynthesizer;
import com.sun.media.sound.SoftSynthesizer;

public class NoteOverFlowTest {

    public static void main(String[] args) throws Exception
    {
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        AudioInputStream stream = synth.openStream(format, null);

        // Make all voices busy, e.g.
        // send midi on and midi off on all available voices
        MidiChannel ch1 = synth.getChannels()[0];
        ch1.programChange(48); // Use contionus instrument like string ensemble
        for (int i = 0; i < synth.getMaxPolyphony(); i++) {
            ch1.noteOn(64, 64);
            ch1.noteOff(64);
        }

        // Now send single midi on, and midi off message
        ch1.noteOn(64, 64);
        ch1.noteOff(64);

        // Read 10 sec from stream, by this time all voices should be inactvie
        stream.skip(format.getFrameSize() * ((int)(format.getFrameRate() * 20)));

        // If no voice are active, then this test will pass
        VoiceStatus[] v = synth.getVoiceStatus();
        for (int i = 0; i < v.length; i++) {
            if(v[i].active)
            {
                throw new RuntimeException("Not all voices are inactive!");
            }
        }

        // Close the synthesizer after use
        synth.close();
    }
}