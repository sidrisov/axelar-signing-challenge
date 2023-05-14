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

	@Lob
	@Column(columnDefinition = "varbinary(32)")
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
