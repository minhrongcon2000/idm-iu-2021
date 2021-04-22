package data;

import weka.core.Instances;

public class DataCleaner {
    private Instances data;

    public DataCleaner(Instances data) {
        this.data = data;
    }

    public DataCleaner removeCTransaction() {
        return new DataCleaner(DataPreprocessing.removeCTransaction(this.data));
    }

    public DataCleaner removeNegativeQuantity() {
        return new DataCleaner(DataPreprocessing.removeNegativeQuantity(this.data));
    }

    public DataCleaner removeInvalidPrice() {
        return new DataCleaner(DataPreprocessing.removeInvalidPrice(this.data));
    }

    public DataCleaner removeMissingID() {
        return new DataCleaner(DataPreprocessing.removeMissingID(this.data));
    }

    public DataCleaner defaultClean() {
        return this.removeCTransaction()
                .removeNegativeQuantity()
                .removeInvalidPrice()
                .removeMissingID();
    }

    public Instances getData() {
        return data;
    }
}
