package ua.sinaver.web3.data;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
// without the index, skip lock gets into race condition,
// resulting in no signing key returned at all, although there are a lot of keys
// not locked
@Table(indexes = @Index(columnList = "last_used"))
public class SigningKey {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	// secp256k1 is not of constant length, typically it's between 32 and 33 bytes,
	// but in standard java API it will be 144 bytes, as it encodes it in PKCS#8
	// format
	// which all the required information, like type of curve, etc.
	// if we need to create too many keys, better switch to 32/33 format, and
	// specify curve spec used
	@Lob
	@Column(columnDefinition = "varbinary(144)")
	private byte[] keyData;

	// specify the name, spring entity manager has issue
	// with creating index without this
	@Column(name = "last_used")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUsed = new Date();

	@Version
	private Long version;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte[] getKeyData() {
		return keyData;
	}

	public void setKeyData(byte[] keyData) {
		this.keyData = keyData;
	}

	public Date getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
