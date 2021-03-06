package org.biojava3.structure.align.symm.benchmark.comparison.order;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biojava3.structure.align.symm.benchmark.Case;
import org.biojava3.structure.align.symm.benchmark.Sample;
import org.biojava3.structure.align.symm.census3.CensusSignificance;
import org.biojava3.structure.align.symm.census3.CensusSignificanceFactory;

/**
 * A class to determine the accuracy of CE-Symm for determining order of rotational symmetry.
 * @author dmyerstu
 * @see AccuracyFinder, which considers only the binary choice of symmetric/asymmetric
 */
public class OrderAccuracy {

	private static final Logger logger = LogManager.getLogger(OrderAccuracy.class.getName());

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: " + OrderAccuracy.class.getSimpleName() + " input-file");
			return;
		}
		File input = new File(args[0]);
		OrderAccuracy finder = new OrderAccuracy(input, CensusSignificanceFactory.forCeSymmTm(), GroupComparisonFactory.exact());
		System.out.println(finder);
	}

	public OrderAccuracy(File input, CensusSignificance sig, GroupComparator guesser) throws IOException {
		this(Sample.fromXML(input), sig, guesser);
	}

	private int correct = 0;
	private int total = 0;
	
	public OrderAccuracy(Sample sample, CensusSignificance sig, GroupComparator guesser) {
		for (Case c : sample.getData()) {
			try {
				if (!c.getKnownInfo().hasRotationalSymmetry()) continue;
				if (!sig.isSignificant(c.getResult())) continue;
				Integer known = c.getKnownOrder();
				if (known == null) known = 1;
				Integer guess = c.getOrder();
				if (guess == null) guess = 1;
				boolean equiv = guesser.hasEquivalentOrder(known, guess);
				if (equiv) correct++;
				total++;
			} catch (RuntimeException e) {
				logger.error("Encountered an error on " + c.getScopId(), e);
			}
		}
	}

	public int getCorrect() {
		return correct;
	}

	public int getTotal() {
		return total;
	}

	public double getAccuracy() {
		return (double) correct / (double) (total);
	}
	
	@Override
	public String toString() {
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(2);
		return nf.format((double) correct / (double) (total) * 100.0) + "%";
	}

}
