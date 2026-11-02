package cbsp.lab04;

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.FilterBandStop;
import com.jsyn.unitgen.FilterPeakingEQ;

/** Tasks 6–7: implement the JSyn methods marked TODO. */
public final class JsynFilters {
    private JsynFilters() { }

    // TODO (Task 6): add a band-stop unit and set its notch frequency and Q.
    public static FilterBandStop bandStop(Synthesizer synth, double frequencyHz, double q) {
        
        FilterBandStop filter = new FilterBandStop();

        synth.add(filter);

        filter.frequency.set(frequencyHz);
        filter.Q.set(q);

        return filter;
    }

    // TODO (Task 7): add a peaking unit and set frequency, Q and LINEAR gain.
    public static FilterPeakingEQ peaking(Synthesizer synth, double frequencyHz, double q, double gainDb) {
        FilterPeakingEQ eq = new FilterPeakingEQ();

        synth.add(eq);


        eq.frequency.set(frequencyHz);
        eq.Q.set(q);
        eq.gain.set(Math.pow(10.0, gainDb / 20.0));


        return eq;
    }

    // TODO (Task 7): connect seven independent EQ units in series; do not sum their outputs.
    public static UnitOutputPort cascade(Synthesizer synth, UnitOutputPort input,
            double[] frequencies, double[] qs, double[] gainsDb) {
        
        UnitOutputPort current = input;


        for (int i = 0; i < frequencies.length; i++) {

            FilterPeakingEQ stage = peaking(synth, frequencies[i], qs[i], gainsDb[i]);

            
            current.connect(stage.input);
            current = stage.output;
        }


        return current;
    }
}
