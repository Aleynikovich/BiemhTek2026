package biemhTekniker.model;

/**
 * POJO representing a program descriptor from the config service.
 * Maps to the JSON response from GET /api/programs/{program_number}.
 */
public class ProgramDescriptor
{
    private Long        id;
    private Integer     programNumber;
    private String      programName;
    private ProgramType programType;
    private String      description;
    private Boolean     enabled;

    // Default constructor required for JSON deserialization
    public ProgramDescriptor()
    {
    }

    public ProgramDescriptor(Integer programNumber, String programName, ProgramType programType, String description)
    {
        this.programNumber = programNumber;
        this.programName   = programName;
        this.programType   = programType;
        this.description   = description;
        this.enabled       = true;
    }

    // Getters and Setters
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getProgramNumber()
    {
        return programNumber;
    }

    public void setProgramNumber(Integer programNumber)
    {
        this.programNumber = programNumber;
    }

    public String getProgramName()
    {
        return programName;
    }

    public void setProgramName(String programName)
    {
        this.programName = programName;
    }

    public ProgramType getProgramType()
    {
        return programType;
    }

    public void setProgramType(ProgramType programType)
    {
        this.programType = programType;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override public String toString()
    {
        return "ProgramDescriptor{" +
                "programNumber=" + programNumber +
                ", programName='" + programName + '\'' +
                ", programType=" + programType +
                ", enabled=" + enabled +
                '}';
    }
}
