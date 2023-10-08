namespace BookAlleyWebApi.Models
{
    public class SessionToken
    {
        public Guid Id { get; set; }
        public required User User { get; set; }
    }
}
