package cbsp.lab04;

import java.util.Arrays;

/** Tasks 1–5 and 8: implement the methods marked TODO.
 * Algorithm inputs are validated by the prepared infrastructure or assumed valid.
 * Coefficient equations: W3C Audio EQ Cookbook (RBJ), Q parametrization.
 */
public final class FrequencyFilters {
    private FrequencyFilters() { }

    // periodic Hann window and calibrated one-sided amplitude spectrum.
    public static Spectrum analyze(double[] samples, int sampleRate, boolean hann) {
        double[] real = new double[samples.length];
        double[] imaginary = new double[samples.length];

        double[] gain = new double[samples.length];
        double wSum = 0.0; 

        for (int n = 0; n<samples.length; n++) {
            if (!hann) {
                gain[n] = 1;
            } else {
                gain[n] = 0.5*(1-Math.cos(2*Math.PI*n/(double) samples.length));
            }

            real[n] = samples[n] * gain[n];
            wSum += gain[n];
        }

        Fft.forward(real, imaginary);

        double[] frequency = new double[samples.length/2 + 1];
        double[] amplitude = new double[samples.length/2 + 1];

        double magnitude = 0.0;


        for (int bin = 0; bin <= samples.length/2; bin++){
            frequency[bin] = ((double) bin * sampleRate) / samples.length;
            
            magnitude = Math.hypot(real[bin], imaginary[bin]);

            if (bin == 0 || bin == samples.length/2) {
                amplitude[bin] = (1 * magnitude) / wSum;
            } else {
                amplitude[bin] = (2 * magnitude) / wSum;
            }
        }

        return new Spectrum(frequency, amplitude);
    }

    public static double[] process(double[] samples, Coefficients c) {
        double [] output = new double[samples.length];

        double x = 0.0;
        double y = 0.0;
        double x1 = 0.0;
        double x2 = 0.0;
        double y1 = 0.0;
        double y2 = 0.0;

        for (int n = 0; n < samples.length; n++) {
            x = samples[n];

            y = c.b0()*x + c.b1()*x1 + c.b2()*x2 - c.a1()*y1 - c.a2()*y2;

            output[n] = y;

            x2 = x1;
            x1 = x;
            y2 = y1;
            y1 = y;
        }

        return output;
        
    }

    public static Coefficients lowPass(double fs, double f0, double q) {
        double w0 = 2*Math.PI*f0/fs;
        double c = Math.cos(w0);
        double alpha = Math.sin(w0) / (2*q);

        double b0 = (1.0 - c) / 2.0;
        double b1 = 1.0 - c;
        double b2 = (1.0 - c) / 2.0;
        double a0 = 1.0 + alpha;
        double a1 = -2.0 * c;
        double a2 = 1.0 - alpha;

        return Coefficients.normalized(b0, b1, b2, a0, a1, a2);

    }

    public static Coefficients highPass(double fs, double f0, double q) {
        double w0 = 2*Math.PI*f0/fs;
        double c = Math.cos(w0);
        double alpha = Math.sin(w0) / (2*q);

        double b0 = (1.0 + c) / 2.0;
        double b1 = -(1.0 + c);
        double b2 = (1.0 + c) / 2.0;
        double a0 = 1.0 + alpha;
        double a1 = -2.0 * c;
        double a2 = 1.0 - alpha;

        return Coefficients.normalized(b0, b1, b2, a0, a1, a2);
    }

    public static Coefficients notch(double fs, double f0, double q) {
        double w0 = 2*Math.PI*f0/fs;
        double c = Math.cos(w0);
        double alpha = Math.sin(w0) / (2*q);

        double b0 = 1.0;
        double b1 = -2.0 * c;
        double b2 = 1.0;
        double a0 = 1.0 + alpha;
        double a1 = -2.0 * c;
        double a2 = 1.0 - alpha;

        return Coefficients.normalized(b0, b1, b2, a0, a1, a2);
    }

    // peaking EQ; gainDb is not a linear amplitude multiplier.
    public static Coefficients peaking(double fs, double f0, double q, double gainDb) {
        double w0 = 2*Math.PI*f0/fs;
        double c = Math.cos(w0);
        double alpha = Math.sin(w0) / (2*q);
        double A = Math.pow(10.0, gainDb / 40.0);

        double b0 = 1.0 + alpha * A;
        double b1 = -2.0 * c;
        double b2 = 1.0 - alpha * A;
        double a0 = 1.0 + alpha / A;
        double a1 = -2.0 * c;
        double a2 = 1.0 - alpha / A;

        return Coefficients.normalized(b0, b1, b2, a0, a1, a2);
    }
    
    // serial cascade; empty cascade returns a distinct unchanged copy.
    public static double[] cascade(double[] samples, Coefficients[] stages) {
        double[] current = samples.clone();

        if (stages.length == 0) {
            return current;
        }

        for (Coefficients stage : stages) {
            current = process(current, stage);
        }

        return current;

    }

    // FFT -> symmetric band-stop mask -> IFFT, without an analysis window.
    public static double[] fftBandStop(double[] samples, int sampleRate, double lowHz, double highHz) {
        
        if (samples.length == 0) {
            return new double[0];
        }

        int n = 2;
        while (n < samples.length) {
            n *= 2;
        }

        double[] re = new double[n];
        double[] im = new double[n];

        System.arraycopy(samples, 0, re, 0, samples.length);

        // 2
        Fft.forward(re, im);



        // 3
        for (int k = 0; k < n; k++) {
            double fAbs = Math.min(k, n - k) * (double) sampleRate / n;

            if (fAbs >= lowHz && fAbs <= highHz) {
                re[k] = 0.0;
                im[k] = 0.0;
            }
        }



        // 4
        Fft.inverse(re, im);    


        // 5
        return Arrays.copyOf(re, samples.length);

    }
}
