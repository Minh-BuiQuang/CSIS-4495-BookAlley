namespace BookAlleyWebApi.Models
{
    public class User
    {
        public long Id { get; set; }
        public required string Name { get; set; }
        public required string Email { get; set; }
        public required string Password { get; set; }
        public virtual ICollection<Conversation> PosterConversations { get; set; }
        public virtual ICollection<Conversation> FinderConversations { get; set; }
    }
}
