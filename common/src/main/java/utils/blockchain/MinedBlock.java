package utils.blockchain;

import utils.crypto.StringUtil;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.min;

/**
 * Immutable Block used in Blockchain.
 */
public final class MinedBlock implements Serializable {
    private static final long serialVersionUID = 3L;
    private final long timeStamp;
    private final long id;
    private final String hash;
    private final String prevHash;
    private final int magicNumber;
    private final List<Transaction> transactions;


    /**
     * Creates <code>MinedBlock</code> from <code>NewBlock</code>.
     * @param block reference to <code>NewBlock</code>.
     * @param id id to be given to this block.
     */
    public MinedBlock(NewBlock block, long id) {
        this.timeStamp = System.currentTimeMillis();
        this.id = id;
        this.prevHash = block.hashOfPrev;
        this.magicNumber = block.magicNumber;
        this.transactions = Collections.unmodifiableList(block.transactions);
        this.hash = applyHash();
    }

    /**
     * Applies SHA-256 Hash on block.
     *
     * @return SHA-256 hash from block data.
     */
    private String applyHash() {
        return StringUtil.applySha256(this.toString());
    }

    /**
     * String contains essential data about block used in hash.
     * <ul>
     *     <li>SHA-256 hash of previous block.</li>
     *     <li>Transactions included in block.</li>
     *     <li>Magic number - number used to force correct number of zeros at start of hash.</li>
     * </ul>
     * @return string with data from block.
     */
    @Override
    public String toString() {
        return prevHash + transactions + magicNumber;
    }

    /**
     * Timestamp contains moment of mining block in milliseconds.
     * Value of timestamp depends on underlying system - {@link System#currentTimeMillis()}.
     * <p>Used to verify if next block is mined after the previous one as it should be.</p>
     * @return timestamp in milliseconds
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * ID of block is number of preceding blocks + 1 in <code>Blockchain</code>.
     * <p>Used to verify if blocks in <code>Blockchain</code> is in proper order</p>
     * @return index of block in <code>Blockchain</code> starting from 1.
     */
    public long getId() {
        return id;
    }

    /**
     * SHA-256 Hash of block consists hash of previous block, all transactions included in block and magic number
     * used to force number of zeros at start of hash.
     * @return hash of block.
     */
    public String getHash() {
        return hash;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    /**
     * Blocks are equal if they are the same object or if they have all the same parameters.
     * @param o object to compare
     * @return <code>true</code> if blocks are equal;<code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinedBlock that = (MinedBlock) o;
        return getTimeStamp() == that.getTimeStamp() && getId() == that.getId() && getMagicNumber() == that.getMagicNumber() && getHash().equals(that.getHash()) && Objects.equals(getPrevHash(), that.getPrevHash()) && Objects.equals(getTransactions(), that.getTransactions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimeStamp(), getId(), getHash(), getPrevHash(), getMagicNumber(), getTransactions());
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getOwner() {
        return transactions.size() > 0 ? Base64.getEncoder().encodeToString(transactions.get(0).getReceiver().getEncoded()) : "null";
    }

    /**
     * Prints block data in pretty form.
     * @return string with prettified data.
     */
    public String toPrettyString() {
        StringJoiner sb = new StringJoiner("\n");
        sb.add("Block:");
        sb.add("Created by " + getOwner());
        sb.add("Miner gets 10 VC");
        sb.add("Id: " + getId());
        sb.add("Timestamp:" + getTimeStamp());
        sb.add("Magic number: " + getMagicNumber());
        sb.add("Hash of the previous block: ");
        sb.add(getPrevHash());
        sb.add("Hash of the block: ");
        sb.add(getHash());
        sb.add("Block data: " + (getTransactions().isEmpty() ? "no messages" : ""));
        getTransactions().stream().map(Transaction::toString).forEach(sb::add);
        sb.add("\n");
        return sb.toString();

    }
}
